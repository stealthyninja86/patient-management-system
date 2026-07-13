package com.pms.notificationservice.dto.request;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;

import java.util.Collections;
import java.util.Map;

public record NotificationRequest (
    String eventId,
    String patientId,
    NotificationType type,
    NotificationChannel channel,
    String recipient,
    String message,
    Map<String, Object> attributes
) {
    public NotificationRequest {
        attributes = attributes != null ? Collections.unmodifiableMap(attributes) : Map.of();
    }
}