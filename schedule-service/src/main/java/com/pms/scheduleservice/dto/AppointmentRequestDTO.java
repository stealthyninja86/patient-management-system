package com.pms.scheduleservice.dto;

public record AppointmentRequestDTO(
    String patientId,
    String timeSlotId
) {}
