package com.pms.scheduleservice.dto;

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
    String hospitalId,
    String hospitalName,
    AppointmentStatus status,
    LocalDateTime createdAt
) {}
