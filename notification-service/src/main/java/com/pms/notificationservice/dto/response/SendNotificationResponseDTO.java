package com.pms.notificationservice.dto.response;

public record SendNotificationResponseDTO(
    boolean sent,
    String message
) {}