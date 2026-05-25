package com.pms.hospitalservice.dto;

import java.util.List;

public record DepartmentRequestDTO(
        String name,
        String hospitalName,
        List<DoctorRequestDTO> doctors
) {}
