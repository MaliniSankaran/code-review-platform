package com.codereview.platform.repository;

import com.codereview.platform.entity.PRStatus;
import com.codereview.platform.entity.PullRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {

    //All PRs in a repo
    List<PullRequest> findByCodeRepositoryId(Long repositoryId);

    //All PRs created by user
    List<PullRequest> findByAuthorId(Long authorId);

    //Filter PRs by status
    List<PullRequest> findByCodeRepositoryIdAndStatus(Long repositoryId, PRStatus status);

}
