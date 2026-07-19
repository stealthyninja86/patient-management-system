package com.pms.scheduleservice.dto.request;

import java.time.LocalDateTime;

public record TimeSlotRequestDTO(
    String doctorId,
    String doctorName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String hospitalId,
    String hospitalName
) {}
