package com.pms.notificationservice.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PrescriptionPdfGeneratedEventDTO (
    String prescriptionId,
    String patientId,
    String patientName,
    String doctorName,
    String hospitalName,
    String patientEmail,
    String status,
    String doctorId,
    String hospitalId
) {
}
