package com.pms.hospitalservice.dto.request;

import com.pms.hospitalservice.service.validation.CreateValidation;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record HospitalRequestDTO(
        @NotBlank(groups = CreateValidation.class, message = "name is required")
        String name,
        String address,
        String website,
        String email,
        String phone,
        List<DepartmentRequestDTO> departments
) {}
