package com.pms.authservice.dto.response;

public record DoctorRegisterResponseDTO(
        String doctorId,
        String email,
        String message
) {}