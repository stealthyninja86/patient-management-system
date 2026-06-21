package com.pms.scheduleservice.dto.response;

public record DoctorPatientDTO(
    String patientId,
    String patientName,
    String patientEmail
) {}