package com.pms.notificationservice.dto.event;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;

public record PrescriptionReadyNotification(
        String eventId,
        String patientId,
        String patientName,
        String doctorName,
        String hospitalName,
        String prescriptionId,
        NotificationType type,
        NotificationChannel channel,
        String recipient,
        String message
) implements NotificationMessage {}
