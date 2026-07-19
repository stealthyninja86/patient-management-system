package com.pms.notificationservice.dto.event;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;

public record AppointmentReminderNotification(
        String eventId,
        String patientId,
        NotificationType type,
        NotificationChannel channel,
        String recipient,
        String message,
        String patientName,
        String doctorName,
        String hospitalName,
        String date,
        String startTime,
        String endTime,
        String appointmentId
) implements NotificationMessage {}
