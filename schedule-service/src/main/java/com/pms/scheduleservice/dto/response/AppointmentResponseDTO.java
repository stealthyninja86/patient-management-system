package com.pms.scheduleservice.dto.response;

import com.pms.scheduleservice.model.AppointmentStatus;
import java.time.LocalDateTime;

public record AppointmentResponseDTO(
    String appointmentId,
    String patientId,
    String patientName,
    String patientEmail,
    String doctorId,
    String doctorName,
    String timeSlotId,
    String timeSlotName,
    AppointmentStatus status,
    LocalDateTime createdAt
) {}
