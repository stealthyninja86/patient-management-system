package com.pms.authservice.dto;

public record DoctorRegisterRequestDTO(
        String name,
        String email,
        String phone,
        String departmentId,
        String hospitalId
) {}
