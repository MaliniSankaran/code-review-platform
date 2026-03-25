package com.codereview.platform.listener;

import com.codereview.platform.event.CommentAddedEvent;
import com.codereview.platform.event.PRCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventListener {

    @EventListener
    public void handlePRCreated(PRCreatedEvent event) {
        log.info("[NOTIFICATION] New PR '{}' (id={}) created in repository {} by {}",
                event.getTitle(),
                event.getPrId(),
                event.getRepositoryId(),
                event.getAuthorUsername()
        );
    }

    @EventListener
    public void handleCommentAdded(CommentAddedEvent event) {
        log.info("[NOTIFICATION] New {} comment (id={}) on PR {} by {}",
                event.isLineComment() ? "line-level" : "PR-level",
                event.getCommentId(),
                event.getPullRequestId(),
                event.getAuthorUsername()
        );
    }
}
