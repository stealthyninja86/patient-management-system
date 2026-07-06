package com.pms.timelineservice.dto.event;

public record ConsentGrantedEvent(
        String patientId,
        String hospitalId,
        int ttlSeconds
){
}
