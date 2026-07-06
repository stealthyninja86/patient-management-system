package com.pms.scheduleservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ConfirmAppointmentRequestDTO (
        @NotBlank String code
){
}
