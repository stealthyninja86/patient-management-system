package com.pms.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(@NotBlank(message = "email is required")
                              @Email(message = "Email should be a valid email")String email,
                              @NotBlank(message =  "password is required")String password) {


}
