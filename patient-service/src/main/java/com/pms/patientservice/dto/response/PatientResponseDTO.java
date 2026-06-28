package com.pms.patientservice.dto.response;

public record PatientResponseDTO(
    String id,
    String patientId,
    String name,
    String email,
    String address,
    String dateOfBirth,
    String registeredDate,
    String phone,
    String gender,
    String bloodType
) {}
