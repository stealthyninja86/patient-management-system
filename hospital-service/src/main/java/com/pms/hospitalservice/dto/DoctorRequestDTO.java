package com.pms.hospitalservice.dto;

public record DoctorRequestDTO(
        String name,
        String hospitalName,
        String departmentName,
        String departmentId,
        String email,
        String phone
) {}
