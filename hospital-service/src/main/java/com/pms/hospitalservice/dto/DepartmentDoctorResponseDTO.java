package com.pms.hospitalservice.dto;

import java.util.List;

public record DepartmentDoctorResponseDTO(
        String departmentId,
        String name,
        String hospitalId,
        String hospitalName,
        List<DoctorResponseDTO> doctors
) {}
