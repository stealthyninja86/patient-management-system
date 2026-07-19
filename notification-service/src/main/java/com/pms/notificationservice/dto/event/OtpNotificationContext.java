package com.pms.notificationservice.dto.event;

public record OtpNotificationContext(
        String domainKey,
        String phoneNumber,
        String email,
        String code
) {
}
