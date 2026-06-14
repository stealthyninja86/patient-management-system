package com.pms.authservice.dto.event;

import java.time.Instant;

public record DoctorRegisteredEvent(
        String email,
        String doctorId,
        String hospitalId,
        Instant timestamp
) {
}
