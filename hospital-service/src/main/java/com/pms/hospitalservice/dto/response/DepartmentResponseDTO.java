package com.pms.hospitalservice.dto.response;

import java.util.List;

public record DepartmentResponseDTO(
        String departmentId,
        String name,
        List<DoctorResponseDTO> doctors
) {}
