package com.pms.clinicalservice.dto.request;

public record PatientContactUpdateDTO(
        String patientPhone,
        String patientEmail
) {}
