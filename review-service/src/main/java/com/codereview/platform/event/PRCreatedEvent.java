package com.codereview.platform.event;

import lombok.Getter;

@Getter
public class PRCreatedEvent {

    private final Long prId;
    private final String title;
    private final Long repositoryId;
    private final Long authorId;
    private final String authorUsername;

    public PRCreatedEvent(Long prId, String title, Long repositoryId, Long authorId, String authorUsername) {
        this.prId = prId;
        this.title = title;
        this.repositoryId = repositoryId;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
    }
}
