package com.codereview.file.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Comment content is required")
    private String content;

    private Long codeFileId;

    private Integer lineNumber;
}
