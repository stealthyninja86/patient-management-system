package com.pms.scheduleservice.dto.request;

public record AppointmentRequestDTO(
    String patientId,
    String timeSlotId
) {}
