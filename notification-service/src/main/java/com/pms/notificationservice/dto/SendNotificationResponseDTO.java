package com.pms.notificationservice.dto;

public record SendNotificationResponseDTO(
    boolean sent,
    String message
) {}