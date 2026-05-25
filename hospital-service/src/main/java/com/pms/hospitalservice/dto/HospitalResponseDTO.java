package com.pms.hospitalservice.dto;

import java.util.List;

public record HospitalResponseDTO(
        String hospitalId,
        String name,
        String address,
        List<DepartmentResponseDTO> departments
) {}
