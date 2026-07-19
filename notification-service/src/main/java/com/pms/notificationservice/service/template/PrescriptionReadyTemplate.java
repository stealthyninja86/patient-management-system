package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.NotificationMessage;
import com.pms.notificationservice.dto.event.PrescriptionReadyNotification;
import com.pms.notificationservice.dto.event.PrescriptionPdfGeneratedEventDTO;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionReadyTemplate extends NotificationMessageTemplate<PrescriptionPdfGeneratedEventDTO> {

    @Override
    public NotificationMessage createRequest(PrescriptionPdfGeneratedEventDTO event, NotificationChannel channel) {
        return new PrescriptionReadyNotification(
                buildDedupKey(event, channel),
                event.patientId(),
                event.patientName(),
                event.doctorName(),
                event.hospitalName(),
                event.prescriptionId(),
                NotificationType.PRESCRIPTION_READY,
                channel,
                resolveRecipient(event, channel),
                buildMessage(event, channel)
        );
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.PRESCRIPTION_READY;
    }

    @Override
    public String getPatientId(PrescriptionPdfGeneratedEventDTO event) {
        return event.patientId();
    }

    @Override
    public String buildMessage(PrescriptionPdfGeneratedEventDTO event, NotificationChannel channel) {
        return String.format(
                "Your prescription (Id: %s) is ready. Please login to view and download",
                event.prescriptionId());
    }

    @Override
    public String resolveRecipient(PrescriptionPdfGeneratedEventDTO event, NotificationChannel channel) {
        return event.patientEmail();
    }

    @Override
    public String buildDedupKey(PrescriptionPdfGeneratedEventDTO event, NotificationChannel channel) {
        return "rx-" + event.prescriptionId() + ":" + channel.name().toLowerCase();
    }
}
