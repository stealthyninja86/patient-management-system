package com.pms.hospitalservice.dto.request;

public record DoctorRequestDTO(
        String name,
        String hospitalName,
        String departmentName,
        String departmentId,
        String email,
        String phone
) {}
