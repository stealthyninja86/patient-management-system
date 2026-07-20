package com.pms.patientservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConsentRequestDTO(
    @NotBlank String patientId,
    @NotBlank String hospitalId,
    String phoneNumber,
    String email
) {}
