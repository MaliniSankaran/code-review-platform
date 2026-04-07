package com.codereview.file.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDTO {

    private Long id;
    private String content;
    private Long pullRequestId;
    private Long codeFileId;
    private String codeFileName;
    private Integer lineNumber;
    private Long authorId;
    private String authorUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
