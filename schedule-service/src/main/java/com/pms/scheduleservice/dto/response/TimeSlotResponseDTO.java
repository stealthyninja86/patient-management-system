package com.pms.scheduleservice.dto.response;

import java.time.LocalDateTime;

public record TimeSlotResponseDTO(
    String timeSlotId,
    String doctorId,
    String doctorName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String status
) {}
