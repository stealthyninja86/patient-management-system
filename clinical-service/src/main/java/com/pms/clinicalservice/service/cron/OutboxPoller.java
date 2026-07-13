package com.pms.clinicalservice.service.cron;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.clinicalservice.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPoller(OutboxRepository outboxRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishPendingEvents() {
        var events = outboxRepository.findUnpublishedEvents();

        if (events.isEmpty()) {
            log.info("No pending events found");
            return;
        }

        for (var event : events) {
            try {
                var payload = objectMapper.readValue(event.getPayload(), Object.class);
                var future = event.getPartitionKey() != null
                        ? kafkaTemplate.send(event.getTopic(), event.getPartitionKey(), payload)
                        : kafkaTemplate.send(event.getTopic(), payload);

                future.get(5, TimeUnit.SECONDS);
                outboxRepository.markPublished(event.getId(), LocalDateTime.now());
            } catch (Exception e) {
                log.error("failed to publish event {} to topic {}: {}", event.getId(), event.getTopic(), e.getMessage());
            }
        }
    }
}
