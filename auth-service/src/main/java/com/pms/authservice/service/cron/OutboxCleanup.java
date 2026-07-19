package com.pms.authservice.service.cron;

import com.pms.authservice.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class OutboxCleanup {
    private static final Logger log = LoggerFactory.getLogger(OutboxCleanup.class);

    private final OutboxRepository outboxRepository;

    public OutboxCleanup(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void deletePublishedEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        outboxRepository.deletePublishedBefore(cutoff);
        log.info("Cleaned up published outbox events older than {}", cutoff);
    }
}
