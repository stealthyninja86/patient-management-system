package com.pms.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // Topics consumed by notification-service (created by other services)
    // appointment-events — created by schedule-service
    // prescription-pdf-events — created by clinical-service

    // No new topics needed — notification-service only consumes existing topics
    // If we later add notification-specific topics:
    // @Bean
    // public NewTopic consentRequestedTopic() {
    //     return TopicBuilder.name("consent-requested")
    //             .partitions(1)
    //             .replicas(1)
    //             .build();
    // }
}
