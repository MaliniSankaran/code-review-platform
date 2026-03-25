package com.codereview.platform.event;

import lombok.Getter;

@Getter
public class CommentAddedEvent {

    private final Long commentId;
    private final Long pullRequestId;
    private final Long authorId;
    private final String authorUsername;
    private final boolean isLineComment;

    public CommentAddedEvent(Long commentId, Long pullRequestId, Long authorId, String authorUsername, boolean isLineComment) {
        this.commentId = commentId;
        this.pullRequestId = pullRequestId;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.isLineComment = isLineComment;
    }
}
