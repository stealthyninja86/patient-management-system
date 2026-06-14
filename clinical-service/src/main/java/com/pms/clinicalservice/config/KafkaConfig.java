package com.pms.clinicalservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic prescriptionPdfTasksTopic() {
        return TopicBuilder.name("prescription-pdf-tasks")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic prescriptionPdfEventsTopic() {
        return TopicBuilder.name("prescription-pdf-events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
