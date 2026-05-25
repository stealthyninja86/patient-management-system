package com.pms.authservice.dto;

public record RegisterRequestDTO(
        String role,
        String hospitalId,
        String departmentId,
        String email,
        String password,
        String name,
        String phone,
        String address,
        String dateOfBirth,
        String gender,
        String bloodType
) {
}
