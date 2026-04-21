package com.codereview.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentAddedEvent {
    private Long commentId;
    private Long pullRequestId;
    private Long authorId;
    private String authorUsername;
    private boolean lineComment;
}