package com.pms.notificationservice.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PrescriptionPdfGeneratedEventDTO (
    String prescriptionId,
    String patientId,
    String patientEmail,
    String status
) {
}
