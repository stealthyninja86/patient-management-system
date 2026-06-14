package com.pms.notificationservice.dto.request;

import java.util.UUID;

public record OtpVerifyRequestDTO(
    UUID otpId,
    String code
) {}