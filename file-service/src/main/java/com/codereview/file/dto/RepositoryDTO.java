package com.codereview.file.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class RepositoryDTO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private String language;
    private Boolean isPublic;
    private Long ownerId;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
