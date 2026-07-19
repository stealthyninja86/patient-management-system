package com.pms.timelineservice.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppointmentEvent(
        String patientId,
        String appointmentId,
        String action,
        String doctorId,
        String doctorName,
        String hospitalId,
        String hospitalName,
        String status,
        String startTime,
        String endTime
){
}
