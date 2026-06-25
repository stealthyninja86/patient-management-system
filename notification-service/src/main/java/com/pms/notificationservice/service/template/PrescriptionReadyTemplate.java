package com.pms.notificationservice.service.template;

import com.pms.notificationservice.dto.event.PrescriptionPdfGeneratedEventDTO;
import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionReadyTemplate extends NotificationMessageTemplate<PrescriptionPdfGeneratedEventDTO> {

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
