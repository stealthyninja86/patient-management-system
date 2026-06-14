package com.pms.authservice.dto.request;

import jakarta.validation.constraints.Size;

public record AdminRegisterRequestDTO(
        String name,
        String email,
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        String phone
) {}