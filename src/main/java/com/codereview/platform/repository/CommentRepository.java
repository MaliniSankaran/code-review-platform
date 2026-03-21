package com.codereview.platform.repository;

import com.codereview.platform.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPullRequestId(Long pullRequestId);

    List<Comment> findByCodeFileId(Long codeFileId);
}
