package com.codereview.file.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePRRequest {

    @NotBlank(message="Title is required")
    private String title;

    private String description;
}
