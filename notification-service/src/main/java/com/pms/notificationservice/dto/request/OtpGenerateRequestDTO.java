package com.pms.notificationservice.dto.request;

public record OtpGenerateRequestDTO(
    String patientId,
    String doctorId,
    String hospitalId,
    String consentRequestId,
    String phoneNumber
) {}