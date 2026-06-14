package com.pms.authservice.dto.response;

public record PatientRegisterResponseDTO(
        String patientId,
        String email,
        String message
) {}