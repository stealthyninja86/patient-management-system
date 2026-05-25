package com.pms.hospitalservice.dto;

import java.util.List;

public record DepartmentResponseDTO(
        String departmentId,
        String name,
        List<DoctorResponseDTO> doctors
) {}
