package com.pms.notificationservice.dto;

import java.util.UUID;

public record OtpVerifyRequestDTO(
    UUID otpId,
    String code
) {}