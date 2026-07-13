package com.pms.authservice.service.cron;

import com.pms.authservice.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public OutboxPoller(OutboxRepository outboxRepository,
                        KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishPendingEvents() {
        var events = outboxRepository.findUnpublishedEvents();
        if (events.isEmpty()) {
            return;
        }

        for (var event : events) {
            try {
                byte[] payload = event.getPayload().getBytes(StandardCharsets.UTF_8);
                var future = event.getPartitionKey() != null
                        ? kafkaTemplate.send(event.getTopic(), event.getPartitionKey(), payload)
                        : kafkaTemplate.send(event.getTopic(), payload);
                future.get(5, TimeUnit.SECONDS);
                outboxRepository.markPublished(event.getId(), LocalDateTime.now());
            } catch (Exception e) {
                log.error("Failed to publish event {} to topic {}: {}", event.getId(), event.getTopic(), e.getMessage());
            }
        }
    }
}
