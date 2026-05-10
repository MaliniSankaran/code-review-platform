package com.codereview.platform.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PRCreatedEvent {

    private  Long prId;
    private  String title;
    private  Long repositoryId;
    private  Long authorId;
    private  String authorUsername;

    public PRCreatedEvent(Long prId, String title, Long repositoryId, Long authorId, String authorUsername) {
        this.prId = prId;
        this.title = title;
        this.repositoryId = repositoryId;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
    }
}
