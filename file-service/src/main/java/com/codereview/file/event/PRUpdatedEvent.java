package com.codereview.file.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PRUpdatedEvent {
    private final Long prId;
    private final String title;
    private final Long repositoryId;
    private final Long authorId;
    private final String authorUsername;
    private final String newStatus;
}