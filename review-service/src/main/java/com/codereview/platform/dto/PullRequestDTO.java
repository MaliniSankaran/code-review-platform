package com.codereview.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class PullRequestDTO implements Serializable {

    private Long id;
    private String title;
    private String description;
    private String status;
    private Long repositoryId;
    private String repositoryName;
    private Long authorId;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
