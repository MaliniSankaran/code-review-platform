package com.codereview.platform.service;

import com.codereview.platform.dto.CreateRepositoryRequest;
import com.codereview.platform.dto.RepositoryDTO;
import com.codereview.platform.entity.CodeRepository;
import com.codereview.platform.entity.User;
import com.codereview.platform.exception.ResourceAlreadyExistsException;
import com.codereview.platform.exception.ResourceNotFoundException;
import com.codereview.platform.repository.RepoRepository;
import com.codereview.platform.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService {
    private final RepoRepository repoRepository;
    private final UserRepository userRepository;

    @CacheEvict(value="owner-repos", key="#ownerId")
    @Transactional
    public RepositoryDTO createRepository(CreateRepositoryRequest request, Long ownerId){

        log.info("Creating repository '{}' for user {} ", request.getName(), ownerId);

        //Find the owner
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ownerId));

        //Check for duplicate name for this user
        if(repoRepository.existsByNameAndOwnerId(request.getName(), ownerId)){
            throw new ResourceAlreadyExistsException("Repository already exists: " + request.getName());
        }

        //Build and sve
        CodeRepository repo = CodeRepository.builder()
                .name(request.getName())
                .description(request.getDescription())
                .language(request.getLanguage())
                .isPublic(request.getIsPublic())
                .owner(owner)
                .build();

        repo = repoRepository.save(repo);

        log.info("Repository created with id: {}", repo.getId());

        return mapToDTO(repo);
    }

    @Cacheable(value="owner-repos", key="#ownerId")
    //Get all repos for a user
    public List<RepositoryDTO> getRepositoriesByOwner(Long ownerId){
        log.info("Fetching repositories for user {}", ownerId);
        List<CodeRepository> repos = repoRepository.findByOwnerId((ownerId));
        return repos.stream()
                .map(this::mapToDTO)
                .toList();
    }

    //Get specific repo by Id
    @Cacheable(value = "repositories", key = "#repoId")
    public RepositoryDTO getRepositoryById(Long repoId){
        log.info("Fetching repository {}",repoId);
        CodeRepository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repoId));
        return mapToDTO(repo);
    }

    @CacheEvict(value = "repositories", key = "#repoId")
    @Transactional
    public RepositoryDTO updateRepository(Long repoId, CreateRepositoryRequest request, Long userId)  {
        log.info("Updating repository {} by user {}", repoId, userId);

        CodeRepository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repoId));

        //Only owner can update
        if(!repo.getOwner().getId().equals(userId)){
            throw new AccessDeniedException("You don't have permission to update this repository");
        }

        repo.setName(request.getName());
        repo.setDescription(request.getDescription());
        repo.setLanguage(request.getLanguage());
        repo.setIsPublic(request.getIsPublic());

        repo = repoRepository.save(repo);

        log.info("Repository {} updated", repoId);
        return mapToDTO(repo);
    }

    @Caching(evict = {
            @CacheEvict(value = "repositories", key = "#repoId"),
            @CacheEvict(value = "owner-repos", key = "#userId")
    })
    @Transactional
    public void deleteRepository(Long repoId, Long userId){
        log.info("Deleting repository {} by user {}", repoId, userId);

        CodeRepository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id: " + repoId));

        if(!repo.getOwner().getId().equals(userId)){
            throw new AccessDeniedException("You don't have permission to delete this repository");
        }
        repoRepository.delete(repo);
        log.info("Repository {} deleted", repoId);
    }

    private RepositoryDTO mapToDTO(CodeRepository repo){
        return RepositoryDTO.builder()
                .id(repo.getId())
                .name(repo.getName())
                .description(repo.getDescription())
                .language(repo.getLanguage())
                .isPublic(repo.getIsPublic())
                .ownerId(repo.getOwner().getId())
                .ownerUsername(repo.getOwner().getUsername())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }
}
