package com.pms.scheduleservice.dto;

public record RescheduleRequestDTO(
        String newTimeSlotId,
        String doctorName,
        String hospitalName,
        String departmentName
) {}
