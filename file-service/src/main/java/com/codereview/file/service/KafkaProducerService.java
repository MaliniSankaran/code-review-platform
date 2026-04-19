package com.codereview.file.service;

import com.codereview.file.config.KafkaTopicConfig;
import com.codereview.file.event.CommentAddedEvent;
import com.codereview.file.event.PRCreatedEvent;
import com.codereview.file.event.PRUpdatedEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostConstruct
    public void init() {
        kafkaTemplate.getProducerFactory().createProducer().close();
        log.info("Kafka producer initialized");
    }

    public void publishPRCreated(PRCreatedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopicConfig.PR_CREATED_TOPIC, event);
            log.info("Published PRCreatedEvent for PR: {}", event.getPrId());
        } catch (Exception e) {
            log.error("Failed to publish PRCreatedEvent: {}", e.getMessage(), e);
        }
    }

    public void publishCommentAdded(CommentAddedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopicConfig.COMMENT_ADDED_TOPIC, event);
            log.info("Published CommentAddedEvent for PR: {}", event.getPullRequestId());
        } catch (Exception e) {
            log.error("Failed to publish PRCreatedEvent: {}", e.getMessage(), e);
        }
    }

    public void publishPRUpdated(PRUpdatedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopicConfig.PR_UPDATED_TOPIC, event);
            log.info("Published PRUpdatedEvent for PR: {} with status: {}",
                    event.getPrId(), event.getNewStatus());
        } catch (Exception e) {
            log.error("Failed to publish PRUpdatedEvent: {}", e.getMessage(), e);
        }
    }
}