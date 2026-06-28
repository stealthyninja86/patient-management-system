package com.pms.scheduleservice.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppointmentEventDTO(
    String eventType,
    String appointmentId,
    String patientId,
    String patientName,
    String patientEmail,
    String patientPhone,
    String doctorId,
    String doctorName,
    String hospitalId,
    String hospitalName,
    String timeSlotId,
    String status,
    String appointmentDate
) {}
