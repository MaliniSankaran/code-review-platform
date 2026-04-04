package com.codereview.platform.repository;

import com.codereview.platform.entity.PRStatus;
import com.codereview.platform.entity.PullRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {

    //All PRs in a repo
    List<PullRequest> findByCodeRepositoryId(Long repositoryId);

    //All PRs created by user
    List<PullRequest> findByAuthorId(Long authorId);

    //Filter PRs by status
    List<PullRequest> findByCodeRepositoryIdAndStatus(Long repositoryId, PRStatus status);

    @Query("SELECT pr FROM PullRequest pr JOIN FETCH pr.author JOIN FETCH pr.codeRepository WHERE pr.codeRepository.id = :repoId")
    List<PullRequest> findByCodeRepositoryIdWithAuthor(@Param("repoId") Long repoId);

    @Query("SELECT pr FROM PullRequest pr JOIN FETCH pr.author JOIN FETCH pr.codeRepository WHERE pr.codeRepository.id = :repoId")
    Page<PullRequest> findByCodeRepositoryIdWithAuthorPaged(@Param("repoId") Long repoId, Pageable pageable);
}
