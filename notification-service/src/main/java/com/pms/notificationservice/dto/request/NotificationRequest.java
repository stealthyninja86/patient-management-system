package com.pms.notificationservice.dto.request;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;

public record NotificationRequest (
    String eventId,
    String patientId,
    NotificationType type,
    NotificationChannel channel,
    String recipient,
    String message
) {}