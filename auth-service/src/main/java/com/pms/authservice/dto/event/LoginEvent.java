package com.pms.authservice.dto.event;

import java.time.Instant;

public record LoginEvent(
        String email,
        String role,
        Instant timestamp,
        String clientIP
) {
}
