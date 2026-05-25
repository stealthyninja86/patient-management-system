package com.pms.scheduleservice.dto;

import com.pms.scheduleservice.model.TimeSlotStatus;
import java.time.LocalDateTime;

public record TimeSlotResponseDTO(
    String timeSlotId,
    String doctorId,
    String doctorName,
    String hospitalId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    TimeSlotStatus status
) {}
