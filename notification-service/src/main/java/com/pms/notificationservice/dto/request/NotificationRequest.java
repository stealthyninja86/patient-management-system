package com.pms.notificationservice.dto.request;

import com.pms.notificationservice.model.NotificationChannel;
import com.pms.notificationservice.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest (
    @NotBlank String eventId,
    @NotBlank String patientId,
    @NotNull NotificationType type,
    @NotNull NotificationChannel channel,
    @NotBlank String recipient,
    @NotBlank String message,
    @NotBlank String patientName,
    @NotBlank String doctorName,
    @NotBlank String hospitalName,
    @NotBlank String date,
    @NotBlank String startTime,
    @NotBlank String endTime,
    @NotBlank String appointmentId
) {}
