package com.pms.notificationservice.dto.event;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;

public record UserOnboardingNotification(
        String eventId,
        String patientId,
        String email,
        String userName,
        String role,
        NotificationType type,
        NotificationChannel channel,
        String recipient,
        String message
) implements NotificationMessage {}
