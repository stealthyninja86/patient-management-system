package com.pms.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppointmentEventDTO(
    @JsonProperty("eventType") String eventType,
    @JsonProperty("appointmentId") String appointmentId,
    @JsonProperty("patientId") String patientId,
    @JsonProperty("patientName") String patientName,
    @JsonProperty("patientEmail") String patientEmail,
    @JsonProperty("patientPhone") String patientPhone,
    @JsonProperty("doctorId") String doctorId,
    @JsonProperty("doctorName") String doctorName,
    @JsonProperty("hospitalId") String hospitalId,
    @JsonProperty("hospitalName") String hospitalName,
    @JsonProperty("status") String status,
    @JsonProperty("appointmentDate") String appointmentDate
) {}