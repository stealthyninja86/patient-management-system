package com.pms.notificationservice.dto;

import com.pms.notificationservice.model.OtpStatus;

public record OtpVerifyResponseDTO(
    boolean verified,
    OtpStatus status,
    String message
) {}