package com.pms.notificationservice.kafka;

import com.pms.notificationservice.dto.event.UserRegistrationEventDTO;
import com.pms.notificationservice.model.NotificationType;
import com.pms.notificationservice.service.NotificationService;
import com.pms.notificationservice.service.strategy.ChannelRouter;
import com.pms.notificationservice.service.template.UserOnboardingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationEventConsumer.class);

    private final NotificationService notificationService;
    private final ChannelRouter channelRouter;
    private final UserOnboardingTemplate onboardingTemplate;

    public UserRegistrationEventConsumer(NotificationService notificationService,
                                          ChannelRouter channelRouter,
                                          UserOnboardingTemplate onboardingTemplate) {
        this.notificationService = notificationService;
        this.channelRouter = channelRouter;
        this.onboardingTemplate = onboardingTemplate;
    }

    @RetryableTopic(
        attempts = "4",
        backoff = @Backoff(delay = 2000, multiplier = 2.0, maxDelay = 30000),
        dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = {
        "user-registrations.admin",
        "user-registrations.doctor",
        "user-registrations.patient"
    }, containerFactory = "userRegistrationKafkaListenerContainerFactory")
    public void consume(UserRegistrationEventDTO event) {
        if (event == null || event.email() == null || event.email().isBlank()) {
            log.warn("Received invalid registration event, skipping");
            return;
        }

        log.info("Received user registration event: email={}", event.email());

        try {
            var requests = onboardingTemplate.createRequests(event,
                    channelRouter.resolve(NotificationType.USER_ONBOARDING));

            for (var request : requests) {
                notificationService.sendNotification(request);
            }

            log.info("Onboarding email sent to: {}", event.email());

        } catch (Exception e) {
            log.error("Failed to process registration event for email={}, error={}",
                event.email(), e.getMessage());
        }
    }

    @DltHandler
    public void handleDlt(UserRegistrationEventDTO event) {
        log.error("Registration event moved to DLT after retries exhausted: email={}",
            event != null ? event.email() : "null");
    }
}
