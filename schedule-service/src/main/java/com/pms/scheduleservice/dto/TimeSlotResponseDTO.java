package com.pms.scheduleservice.dto;

import java.time.LocalDateTime;

public record TimeSlotResponseDTO(
    String timeSlotId,
    String doctorId,
    String doctorName,
    LocalDateTime startTime,
    LocalDateTime endTime
) {}
