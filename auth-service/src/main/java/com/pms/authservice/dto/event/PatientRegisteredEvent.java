package com.pms.authservice.dto.event;

import java.time.Instant;

public record PatientRegisteredEvent(
        String email,
        String patientId,
        Instant timestamp
) {
}
