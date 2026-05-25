package com.pms.patient_service.dto;

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
