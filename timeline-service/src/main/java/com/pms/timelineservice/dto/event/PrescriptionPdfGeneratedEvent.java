package com.pms.timelineservice.dto.event;

import java.util.Map;

public record PrescriptionPdfGeneratedEvent(
        String patientId,
        String appointmentId,
        String prescriptionId,
        String status,
        Map<String, Object> metadata
) {
}
