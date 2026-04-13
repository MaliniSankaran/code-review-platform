package com.codereview.file.service;

import com.codereview.file.dto.CodeFileDTO;
import com.codereview.file.entity.CodeFile;
import com.codereview.file.entity.CodeRepository;
import  com.codereview.common.entity.User;
import  com.codereview.common.exception.ResourceNotFoundException;
import com.codereview.file.processor.FileProcessor;
import com.codereview.file.processor.FileProcessorFactory;
import com.codereview.file.repository.CodeFileRepository;
import com.codereview.file.repository.RepoRepository;
import com.codereview.file.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class CodeFileService {

    private final CodeFileRepository codeFileRepository;
    private final RepoRepository repoRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final FileProcessorFactory fileProcessorFactory;

    @Transactional
    public CodeFileDTO uploadFile(Long repoId, Long userId, MultipartFile file) {

        log.info("Uploading file '{}' to repo {} by user {}", file.getOriginalFilename(), repoId, userId);

        //Verify repo exists
        CodeRepository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id " + repoId));

        //Only repo owner can upload
        if (!repo.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to upload to this repository");
        }
            // Verify user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));


            //Build the MinIO path : repos/{repoId}/{fileName}
            String path = "repos/" + repoId + "/" + file.getOriginalFilename();

            //Process file with factory pattern
            String filename = file.getOriginalFilename();

            if(filename!=null && fileProcessorFactory.isSupported(filename)){
                try{
                    FileProcessor processor = fileProcessorFactory.getFileProcessor(filename);
                    String content = new String(file.getBytes());

                    if(!processor.isValidFile(content)){
                        log.warn("File {} failed validation", filename);
                    }

                    Map<String, Object> metadata = processor.extractMetadata(content);
                    log.info("Extracted metadata for {}: {}", filename, metadata);

                } catch (IOException e) {
                    log.error("Error reading file content for processing: {}", e.getMessage());
                }
            }

            //Store bytes in MinIO
            fileStorageService.uploadFile(path, file);


            //Save metadata in PostgreSQL
            CodeFile codeFile = CodeFile.builder()
                    .fileName(file.getOriginalFilename())
                    .filePath(path)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .codeRepository(repo)
                    .uploadedBy(user)
                    .build();

            codeFile = codeFileRepository.save(codeFile);

            log.info("File uploaded with id: {}", codeFile.getId());
            return mapToDTO(codeFile);

        }

    public List<CodeFileDTO> getFilesByRepository(Long repoId) {

        log.info("Fetching files for repo {}", repoId);

        //Verify repo exists
        repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id " + repoId));

        List<CodeFile> files = codeFileRepository.findByCodeRepositoryId(repoId);
        return files.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public CodeFileDTO getFileById(Long fileId) {
        log.info("Fetching file {}", fileId);
        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));
        return mapToDTO(file);
    }

    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        log.info("Deleting file {} by user {}", fileId, userId);

        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

        // Only repo owner can delete files
        if (!file.getCodeRepository().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to delete this file");
        }

        // Delete from MinIO first, then from database
        fileStorageService.deleteFile(file.getFilePath());
        codeFileRepository.delete(file);

        log.info("File {} deleted", fileId);
    }

    private CodeFileDTO mapToDTO (CodeFile codeFile){
        return CodeFileDTO.builder()
                .id(codeFile.getId())
                .fileName(codeFile.getFileName())
                .filePath(codeFile.getFilePath())
                .contentType(codeFile.getContentType())
                .size(codeFile.getSize())
                .repositoryId(codeFile.getCodeRepository().getId())
                .repositoryName(codeFile.getCodeRepository().getName())
                .uploadedById(codeFile.getUploadedBy().getId())
                .uploadedByUsername(codeFile.getUploadedBy().getUsername())
                .createdAt(codeFile.getCreatedAt())
                .updatedAt(codeFile.getUpdatedAt())
                .build();
    }

}