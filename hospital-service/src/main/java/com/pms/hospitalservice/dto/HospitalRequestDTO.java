package com.pms.hospitalservice.dto;

import com.pms.hospitalservice.validation.CreateValidation;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record HospitalRequestDTO(
        @NotBlank(groups = CreateValidation.class, message = "name is required")
        String name,
        String address,
        List<DepartmentRequestDTO> departments
) {}
