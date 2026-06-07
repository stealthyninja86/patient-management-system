package com.pms.notificationservice.kafka;

import com.pms.notificationservice.dto.AppointmentEventDTO;
import com.pms.notificationservice.dto.NotificationRequest;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import com.pms.notificationservice.service.NotificationService;
import com.pms.notificationservice.strategy.ChannelRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AppointmentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventConsumer.class);

    private final NotificationService notificationService;
    private final ChannelRouter channelRouter;

    public AppointmentEventConsumer(NotificationService notificationService,
                                    ChannelRouter channelRouter) {
        this.notificationService = notificationService;
        this.channelRouter = channelRouter;
    }

    @KafkaListener(topics = "appointment-events")
    public void consume(AppointmentEventDTO event) {
        log.info("Received appointment event: appointmentId={}, status={}",
            event.appointmentId(), event.status());

        try {
            if (!"BOOKED".equals(event.status())) {
                return;
            }

            String eventId = "appt-" + event.appointmentId();
            NotificationType type = NotificationType.APPOINTMENT_CONFIRMATION;

            for (NotificationChannel channel : channelRouter.resolve(type)) {
                String message = buildMessage(channel, event);
                String dedupKey = eventId + ":" + channel.name().toLowerCase();

                String recipient = channel == NotificationChannel.SMS
                    ? event.patientPhone() : event.patientEmail();

                notificationService.sendNotification(new NotificationRequest(
                    dedupKey, event.patientId(), type, channel,
                    recipient, message
                ));
            }

            log.info("Appointment confirmation sent: appointmentId={}", event.appointmentId());

        } catch (Exception e) {
            log.error("Failed to process appointment event: appointmentId={}, error={}",
                event.appointmentId(), e.getMessage());
        }
    }

    private String buildMessage(NotificationChannel channel, AppointmentEventDTO event) {
        if (channel == NotificationChannel.SMS) {
            return String.format(
                "Appointment confirmed with Dr. %s on %s.",
                event.doctorName(), event.appointmentDate());
        }
        return String.format(
            "Dear %s,\n\nYour appointment with Dr. %s at %s has been confirmed.\n" +
            "Date & Time: %s\n\nThank you,\nPatient Management System",
            event.patientName(), event.doctorName(),
            event.hospitalName(), event.appointmentDate());
    }
}
