package com.pms.notificationservice.dto.response;

public record OtpVerifyResult(
        boolean verified,
        String status,
        int attemptsRemaining
) {
}
