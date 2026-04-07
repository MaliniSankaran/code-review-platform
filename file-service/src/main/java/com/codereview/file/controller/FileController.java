package com.codereview.file.controller;

import com.codereview.file.dto.CodeFileDTO;
import com.codereview.file.security.UserPrincipal;
import com.codereview.file.service.CodeFileService;
import com.codereview.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/repositories/{repoId}/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final CodeFileService codeFileService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<CodeFileDTO> uploadFile(
            @PathVariable Long repoId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        CodeFileDTO codeFile = codeFileService.uploadFile(repoId, userPrincipal.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(codeFile);
    }

    @GetMapping
    public ResponseEntity<List<CodeFileDTO>> getFiles(@PathVariable Long repoId) {
        List<CodeFileDTO> files = codeFileService.getFilesByRepository(repoId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<CodeFileDTO> getFile(@PathVariable Long fileId) {
        CodeFileDTO file = codeFileService.getFileById(fileId);
        return ResponseEntity.ok(file);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long fileId) {
        CodeFileDTO file = codeFileService.getFileById(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(new InputStreamResource(fileStorageService.downloadFile(file.getFilePath())));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        codeFileService.deleteFile(fileId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
