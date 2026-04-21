package com.codereview.notification.listener;

import com.codereview.notification.event.CommentAddedEvent;
import com.codereview.notification.event.PRCreatedEvent;
import com.codereview.notification.event.PRUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "pr-created", groupId = "notification-group")
    public void handlePRCreated(String message) throws Exception {
        PRCreatedEvent event = objectMapper.readValue(message, PRCreatedEvent.class);
        log.info("[NOTIFICATION] New PR '{}' (id={}) created in repository {} by {}",
                event.getTitle(), event.getPrId(), event.getRepositoryId(), event.getAuthorUsername());
    }

    @KafkaListener(topics = "comment-added", groupId = "notification-group")
    public void handleCommentAdded(String message) throws Exception {
        CommentAddedEvent event = objectMapper.readValue(message, CommentAddedEvent.class);
        log.info("[NOTIFICATION] New {} comment (id={}) on PR {} by {}",
                event.isLineComment() ? "line" : "PR-level",
                event.getCommentId(), event.getPullRequestId(), event.getAuthorUsername());
    }

    @KafkaListener(topics = "pr-updated", groupId = "notification-group")
    public void handlePRUpdated(String message) throws Exception {
        PRUpdatedEvent event = objectMapper.readValue(message, PRUpdatedEvent.class);
        log.info("[NOTIFICATION] PR '{}' (id={}) status changed to {} by {}",
                event.getTitle(), event.getPrId(), event.getNewStatus(), event.getAuthorUsername());
    }

}