package com.codereview.file.service;


import com.codereview.file.dto.CreatePRRequest;
import com.codereview.file.dto.PullRequestDTO;
import com.codereview.file.entity.CodeRepository;
import com.codereview.file.entity.PRStatus;
import com.codereview.file.entity.PullRequest;
import  com.codereview.common.entity.User;
import com.codereview.file.event.PRCreatedEvent;
import  com.codereview.common.exception.ResourceNotFoundException;
import com.codereview.file.event.PRUpdatedEvent;
import com.codereview.file.repository.PullRequestRepository;
import com.codereview.file.repository.RepoRepository;
import com.codereview.file.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ApplicationEventPublisher eventPublisher;
    private final KafkaProducerService kafkaProducerService;


    //Create method
    @CacheEvict(value = "repo-prs", key = "#repoId")
    @Transactional
    public PullRequestDTO createPullRequest(Long repoId, Long authorId, CreatePRRequest request) {

        log.info("Creating PR '{}' in repo {} by user {}", request.getTitle(), repoId, authorId);

        CodeRepository repo = repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id " + repoId));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + authorId));

        PullRequest pr = PullRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .codeRepository(repo)
                .author(author)
                .build();

        pr = pullRequestRepository.save(pr);

        log.info("PR created with id: {}", pr.getId());

        PRCreatedEvent event = new PRCreatedEvent(
                pr.getId(),
                pr.getTitle(),
                repoId,
                authorId,
                pr.getAuthor().getUsername()
        );

        // In-process (Observer pattern)
        eventPublisher.publishEvent(event);

        // Distributed (Kafka)
        kafkaProducerService.publishPRCreated(event);

        return mapToDTO(pr);
    }

    //Read methods
    @Cacheable(value = "repo-prs", key = "#repoId")
    public List<PullRequestDTO> getPRsByRepository(Long repoId) {

        log.info("Fetching PRs for repo {}", repoId);

        repoRepository.findById(repoId)
                .orElseThrow(()->new ResourceNotFoundException("Repository not found with id " + repoId));

        return pullRequestRepository.findByCodeRepositoryIdWithAuthor(repoId).stream()
                .map(this::mapToDTO)
                .toList();

    }

    public Page<PullRequestDTO> getPRsByRepositoryPaged(Long repoId, Pageable pageable) {
        log.info("Fetching paged PRs for repo {}", repoId);

        repoRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found with id " + repoId));

        return pullRequestRepository.findByCodeRepositoryIdWithAuthorPaged(repoId, pageable)
                .map(this::mapToDTO);
    }

    public List<PullRequestDTO> getPRsByAuthor(Long authorId) {
        log.info("Fetching PRs by user {}", authorId);

        return pullRequestRepository.findByAuthorId(authorId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Cacheable(value="pull-requests", key ="#prId")
    public PullRequestDTO getPRById(Long prId){
        log.info("Fetching PRs  {}", prId);
        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(()->new ResourceNotFoundException("PullRequest not found with id " + prId));
        return mapToDTO(pr);
    }

    //Update PR status
    @CacheEvict(value = "pull-requests", key = "#prId")
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

        PRUpdatedEvent event = new PRUpdatedEvent(
                pr.getId(),
                pr.getTitle(),
                pr.getCodeRepository().getId(),
                pr.getAuthor().getId(),
                pr.getAuthor().getUsername(),
                newStatus
        );

        kafkaProducerService.publishPRUpdated(event);

        return mapToDTO(pr);
    }

    //Delete method
    @CacheEvict(value = "pull-requests", key = "#prId")
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
