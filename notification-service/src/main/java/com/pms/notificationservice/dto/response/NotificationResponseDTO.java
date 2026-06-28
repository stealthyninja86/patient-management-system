package com.pms.notificationservice.dto.response;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationStatus;
import com.pms.notificationservice.model.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDTO(
    UUID id,
    String patientId,
    NotificationType type,
    NotificationChannel channel,
    String recipient,
    String message,
    NotificationStatus status,
    int retryCount,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime sentAt
) {}
