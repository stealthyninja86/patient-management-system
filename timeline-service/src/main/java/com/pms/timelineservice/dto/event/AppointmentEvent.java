package com.pms.timelineservice.dto.event;

public record AppointmentEvent(
        String patientId,
        String appointmentId,
        String action,
        String doctorName,
        String hospitalName,
        String status,
        String startTime,
        String endTime
){
}
