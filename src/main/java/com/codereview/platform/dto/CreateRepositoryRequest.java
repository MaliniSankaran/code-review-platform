package com.codereview.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRepositoryRequest {

    @NotBlank(message="Repository name is required")
    private String name;

    private String description;

    @NotBlank(message="Language is required")
    private String language;

    private Boolean isPublic = true;
}
