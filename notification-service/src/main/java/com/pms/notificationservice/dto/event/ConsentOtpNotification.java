package com.pms.notificationservice.dto.event;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;

public record ConsentOtpNotification(
        String eventId, String patientId, NotificationType type,
        NotificationChannel channel, String recipient, String message,
        String code, String domainKey, String otpType
) implements NotificationMessage {}
