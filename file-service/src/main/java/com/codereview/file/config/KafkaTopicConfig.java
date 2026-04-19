package com.codereview.file.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String PR_CREATED_TOPIC = "pr-created";
    public static final String COMMENT_ADDED_TOPIC = "comment-added";
    public static final String PR_UPDATED_TOPIC = "pr-updated";

    @Bean
    public NewTopic prCreatedTopic() {
        return TopicBuilder.name(PR_CREATED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic commentAddedTopic() {
        return TopicBuilder.name(COMMENT_ADDED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic prUpdatedTopic() {
        return TopicBuilder.name(PR_UPDATED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}