package com.pms.patientservice.dto.event;

public record ConsentGrantedEvent(
    String patientId,
    String hospitalId,
    int ttlSeconds
) {}
