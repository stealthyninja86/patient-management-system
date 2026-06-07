package com.pms.clinicalservice.dto;

public record HospitalContactUpdateDTO(
        String hospitalPhone,
        String hospitalEmail,
        String hospitalWebsite
) {}
