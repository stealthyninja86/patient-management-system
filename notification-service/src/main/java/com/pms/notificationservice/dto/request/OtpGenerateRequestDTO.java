package com.pms.notificationservice.dto.request;

public record OtpGenerateRequestDTO(
    String domainKey,
    String phoneNumber
) {}