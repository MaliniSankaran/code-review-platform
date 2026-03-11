package com.codereview.platform.controller;

import com.codereview.platform.dto.CreateRepositoryRequest;
import com.codereview.platform.dto.RepositoryDTO;
import com.codereview.platform.security.UserPrincipal;
import com.codereview.platform.service.RepositoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
@Slf4j
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping
    public ResponseEntity<RepositoryDTO> createRepository(
            @Valid @RequestBody CreateRepositoryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        log.info("Create repository request from user {}", userPrincipal.getId());
        RepositoryDTO repo = repositoryService.createRepository(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo);
    }

    @GetMapping
    public ResponseEntity<List<RepositoryDTO>> getRepositories(@AuthenticationPrincipal UserPrincipal userPrincipal){
        List<RepositoryDTO> repos = repositoryService.getRepositoriesByOwner(userPrincipal.getId());
        return ResponseEntity.ok(repos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepositoryDTO> getRepository(@PathVariable Long id){
        RepositoryDTO repo = repositoryService.getRepositoryById(id);
        return ResponseEntity.ok(repo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepositoryDTO> updateRepository(
            @PathVariable Long id,
            @Valid @RequestBody CreateRepositoryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        RepositoryDTO repo = repositoryService.updateRepository(id, request, userPrincipal.getId());
        return ResponseEntity.ok(repo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepository(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        repositoryService.deleteRepository(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

}
