package com.pms.hospitalservice.dto;

public record DoctorResponseDTO(
    String doctorId,
    String name,
    String email,
    String phone,
    String departmentId,
    String departmentName
) {
    public String getFullName() {
        return name;
    }
}
