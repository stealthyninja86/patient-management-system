package com.pms.clinicalservice.dto.request;

public record HospitalContactUpdateDTO(
        String hospitalPhone,
        String hospitalEmail,
        String hospitalWebsite
) {}
