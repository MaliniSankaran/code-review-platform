package com.codereview.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CodeFileDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private String contentType;
    private Long size;
    private Long repositoryId;
    private String repositoryName;
    private Long uploadedById;
    private String uploadedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
