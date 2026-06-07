package com.pms.notificationservice.dto;

public record OtpGenerateRequestDTO(
    String patientId,
    String doctorId,
    String hospitalId,
    String consentRequestId,
    String phoneNumber
) {}