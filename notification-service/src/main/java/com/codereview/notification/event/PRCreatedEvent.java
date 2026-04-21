package com.codereview.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PRCreatedEvent {
    private Long prId;
    private String title;
    private Long repositoryId;
    private Long authorId;
    private String authorUsername;
}