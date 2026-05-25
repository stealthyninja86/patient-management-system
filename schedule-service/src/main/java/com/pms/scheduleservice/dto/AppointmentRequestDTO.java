package com.pms.scheduleservice.dto;

public record AppointmentRequestDTO(
    String patientId,
    String patientName,
    String patientEmail,
    String doctorId,
    String departmentId,
    String timeSlotId,
    String hospitalId,
    String hospitalName,
    String reason
) {}
