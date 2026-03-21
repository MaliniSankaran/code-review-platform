package com.codereview.platform.service;


import com.codereview.platform.dto.CreatePRRequest;
import com.codereview.platform.dto.PullRequestDTO;
import com.codereview.platform.entity.CodeRepository;
import com.codereview.platform.entity.PRStatus;
import com.codereview.platform.entity.PullRequest;
import com.codereview.platform.entity.User;
import com.codereview.platform.exception.ResourceNotFoundException;
import com.codereview.platform.repository.PullRequestRepository;
import com.codereview.platform.repository.RepoRepository;
import com.codereview.platform.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PullRequestService {

    private final PullRequestRepository pullRequestRepository;
    private final RepoRepository repoRepository;
    private final UserRepository userRepository;

    //Create method
    public PullRequestDTO createPullRequest(Long repoId, Long authorId, CreatePRRequest request) {

        log.info("Creating PR '{}' in repo {} by user {}", request.getTitle(), repoId, authorId);

        CodeRepository repo = repoRepository.findById(repoId)
                .orElseThrow(()->new ResourceNotFoundException("Repository not found with id " + repoId));

        User author = userRepository.findById(authorId)
                .orElseThrow(()->new ResourceNotFoundException("User not found with id " + authorId));

        PullRequest pr = PullRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .codeRepository(repo)
                .author(author)
                .build();

        pr = pullRequestRepository.save(pr);

        log.info("PR created with id: {}", pr.getId());
        return mapToDTO(pr);
    }

    //Read methods
    public List<PullRequestDTO> getPRsByRepository(Long repoId) {

        log.info("Fetching PRs for repo {}", repoId);

        repoRepository.findById(repoId)
                .orElseThrow(()->new ResourceNotFoundException("Repository not found with id " + repoId));

        return pullRequestRepository.findByCodeRepositoryId(repoId).stream()
                .map(this::mapToDTO)
                .toList();

    }

    public List<PullRequestDTO> getPRsByAuthor(Long authorId) {
        log.info("Fetching PRs by user {}", authorId);

        return pullRequestRepository.findByAuthorId(authorId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public PullRequestDTO getPRById(Long prId){
        log.info("Fetching PRs  {}", prId);
        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(()->new ResourceNotFoundException("PullRequest not found with id " + prId));
        return mapToDTO(pr);
    }

    //Update PR status
    @Transactional
    public PullRequestDTO updatePRStatus(Long prId, String newStatus, Long userId){

        log.info("Updating PR {} status to {} by user {}", prId, newStatus,userId);

        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(()->new ResourceNotFoundException("PullRequest not found with id " + prId));

        if(!pr.getAuthor().getId().equals(userId)){
            throw new AccessDeniedException("You don't have permission to update this pull request");
        }

        try{
            PRStatus status = PRStatus.valueOf(newStatus.toUpperCase());
            pr.setStatus(status);
        } catch(IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid status " + newStatus + ". Must be OPEN, CLOSED, or MERGED");
            }
        pr = pullRequestRepository.save(pr);
        log.info("PR {} status updated to {}", prId, newStatus);
        return mapToDTO(pr);
    }

    //Delete method
    @Transactional
    public void deletePR(Long prId, Long userId) {
        log.info("Deleting PR {} by user {}", prId, userId);

        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> new ResourceNotFoundException("Pull request not found with id: " + prId));

        if (!pr.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("You don't have permission to delete this pull request");
        }

        pullRequestRepository.delete(pr);

        log.info("PR {} deleted", prId);
    }

    private PullRequestDTO mapToDTO(PullRequest pr) {

        return PullRequestDTO.builder()
                .id(pr.getId())
                .title(pr.getTitle())
                .description(pr.getDescription())
                .status(pr.getStatus().name())
                .repositoryId(pr.getCodeRepository().getId())
                .repositoryName(pr.getCodeRepository().getName())
                .authorId(pr.getAuthor().getId())
                .authorUsername(pr.getAuthor().getUsername())
                .createdAt(pr.getCreatedAt())
                .updatedAt(pr.getUpdatedAt())
                .build();
    }
}
