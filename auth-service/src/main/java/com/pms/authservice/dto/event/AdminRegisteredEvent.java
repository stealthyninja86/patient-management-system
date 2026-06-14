package com.pms.authservice.dto.event;

import java.time.Instant;

public record AdminRegisteredEvent(
        String email,
        Instant timestamp
) {
}
