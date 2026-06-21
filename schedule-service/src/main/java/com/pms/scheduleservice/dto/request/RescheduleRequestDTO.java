package com.pms.scheduleservice.dto.request;

public record RescheduleRequestDTO(
        String newTimeSlotId,
        String doctorName,
        String hospitalName,
        String departmentName
) {}
