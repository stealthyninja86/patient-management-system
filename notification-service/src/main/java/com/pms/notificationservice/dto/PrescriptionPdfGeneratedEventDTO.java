package com.pms.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PrescriptionPdfGeneratedEventDTO (
        @JsonProperty("eventId") String eventId,
        @JsonProperty("prescriptionId") String prescriptionId,
        @JsonProperty("patientId") String patientId,
        @JsonProperty("patientEmail") String patientEmail,
        @JsonProperty("doctorId") String doctorId,
        @JsonProperty("hospitalId") String hospitalId,
        @JsonProperty("status") String status,
        @JsonProperty("generatedAt") String generatedAt
) {
}
