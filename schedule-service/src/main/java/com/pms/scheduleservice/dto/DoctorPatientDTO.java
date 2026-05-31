package com.pms.scheduleservice.dto;

public record DoctorPatientDTO(
    String patientId,
    String patientName,
    String patientEmail
) {}