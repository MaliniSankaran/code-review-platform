package com.codereview.platform.controller;

import com.codereview.platform.dto.CreatePRRequest;
import com.codereview.platform.dto.PullRequestDTO;
import com.codereview.platform.security.UserPrincipal;
import com.codereview.platform.service.PullRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
