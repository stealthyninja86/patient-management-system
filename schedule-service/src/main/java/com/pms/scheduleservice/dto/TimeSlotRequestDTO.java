package com.pms.scheduleservice.dto;

import java.time.LocalDateTime;

public record TimeSlotRequestDTO(
    String doctorId,
    String doctorName,
    String hospitalId,
    LocalDateTime startTime,
    LocalDateTime endTime
) {}
