package com.codereview.file.controller;

import com.codereview.file.dto.CreatePRRequest;
import com.codereview.file.dto.PullRequestDTO;
import com.codereview.file.security.UserPrincipal;
import com.codereview.file.service.PullRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories/{repoId}/pulls")
@RequiredArgsConstructor
@Slf4j
public class PullRequestController {

    private final PullRequestService pullRequestService;

    @PostMapping
    public ResponseEntity<PullRequestDTO> createPR(
            @PathVariable Long repoId,
            @Valid @RequestBody CreatePRRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        PullRequestDTO pr = pullRequestService.createPullRequest(repoId, userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pr);
    }
    @GetMapping
    public ResponseEntity<List<PullRequestDTO>> getPRsByRepo(@PathVariable Long repoId) {
        List<PullRequestDTO> prs = pullRequestService.getPRsByRepository(repoId);
        return ResponseEntity.ok(prs);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<PullRequestDTO>> getPRsByRepoPaged(
            @PathVariable Long repoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(pullRequestService.getPRsByRepositoryPaged(repoId, pageable));
    }

    @GetMapping("/{prId}")
    public ResponseEntity<PullRequestDTO> getPR(@PathVariable Long prId) {
        PullRequestDTO pr = pullRequestService.getPRById(prId);
        return ResponseEntity.ok(pr);
    }

    @PatchMapping("/{prId}/status")
    public ResponseEntity<PullRequestDTO> updatePRStatus(
            @PathVariable Long prId,
            @RequestParam("status") String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        PullRequestDTO pr = pullRequestService.updatePRStatus(prId, status, userPrincipal.getId());
        return ResponseEntity.ok(pr);
    }

    @DeleteMapping("/{prId}")
    public ResponseEntity<Void> deletePR(
            @PathVariable Long prId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        pullRequestService.deletePR(prId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }



}
