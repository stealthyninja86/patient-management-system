package com.pms.notificationservice.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserRegistrationEventDTO(
    String email,
    String role,
    String timestamp
) {}
