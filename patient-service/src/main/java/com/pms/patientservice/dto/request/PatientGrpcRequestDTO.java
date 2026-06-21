package com.pms.patientservice.dto.request;

public record PatientGrpcRequestDTO(
    String name,
    String email,
    String phone,
    String address,
    String dateOfBirth,
    String gender,
    String bloodType,
    String registeredDate
) {}
