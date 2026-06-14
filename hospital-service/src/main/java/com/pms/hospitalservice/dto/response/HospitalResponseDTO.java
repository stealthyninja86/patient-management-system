package com.pms.hospitalservice.dto.response;

import java.util.List;

public record HospitalResponseDTO(
        String hospitalId,
        String name,
        String address,
        String website,
        String email,
        String phone,
        List<DepartmentResponseDTO> departments
) {}
