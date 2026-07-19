package com.pms.patientservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmConsentRequestDTO(
    @NotBlank String code
) {}
