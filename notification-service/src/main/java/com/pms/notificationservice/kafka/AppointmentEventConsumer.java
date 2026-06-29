package com.pms.notificationservice.kafka;

import com.pms.notificationservice.dto.event.AppointmentEventDTO;
import com.pms.notificationservice.model.NotificationType;
import com.pms.notificationservice.service.NotificationService;
import com.pms.notificationservice.service.strategy.ChannelRouter;
import com.pms.notificationservice.service.template.AppointmentConfirmationTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class AppointmentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventConsumer.class);

    private final NotificationService notificationService;
    private final ChannelRouter channelRouter;
    private final AppointmentConfirmationTemplate appointmentTemplate;

    public AppointmentEventConsumer(NotificationService notificationService,
                                    ChannelRouter channelRouter,
                                    AppointmentConfirmationTemplate appointmentTemplate) {
        this.notificationService = notificationService;
        this.channelRouter = channelRouter;
        this.appointmentTemplate = appointmentTemplate;
    }

    @RetryableTopic(
        attempts = "4",
        backoff = @Backoff(delay = 2000, multiplier = 2.0, maxDelay = 30000),
        dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = "appointment-events", containerFactory = "appointmentKafkaListenerContainerFactory")
    public void consume(AppointmentEventDTO event) {
        if (event == null) {
            log.warn("Received null appointment event, skipping");
            return;
        }

        log.info("Received appointment event: appointmentId={}, status={}",
            event.appointmentId(), event.status());

        try {
            if (!"BOOKED".equals(event.status())) {
                return;
            }

            var requests = appointmentTemplate.createRequests(event,
                    channelRouter.resolve(NotificationType.APPOINTMENT_CONFIRMATION));

            for (var request : requests) {
                notificationService.sendNotification(request);
            }

            log.info("Appointment confirmation sent: appointmentId={}", event.appointmentId());

        } catch (Exception e) {
            log.error("Failed to process appointment event: appointmentId={}, error={}",
                event != null ? event.appointmentId() : "null", e.getMessage());
        }
    }

    @DltHandler
    public void handleDlt(AppointmentEventDTO event) {
        log.error("Appointment event moved to DLT after retries exhausted: appointmentId={}", event != null ? event.appointmentId() : "null");
    }
}
