package com.pms.scheduleservice.factory;

import com.pms.scheduleservice.dto.AppointmentEventDTO;
import com.pms.scheduleservice.dto.AppointmentRequestDTO;
import com.pms.scheduleservice.dto.AppointmentResponseDTO;
import com.pms.scheduleservice.model.Appointment;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.model.TimeSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class AppointmentFactory {

    private static final Logger log = LoggerFactory.getLogger(AppointmentFactory.class);

    public Appointment toEntity(AppointmentRequestDTO request, String appointmentId, TimeSlot timeSlot) {
        log.debug("Converting AppointmentRequestDTO to entity for appointmentId: {}", appointmentId);
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setPatientId(request.patientId());
        appointment.setDoctorId(timeSlot.getDoctorId());
        appointment.setDoctorName(timeSlot.getDoctorName());
        appointment.setTimeSlotId(request.timeSlotId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
        appointment.setTimeSlotName(
            timeSlot.getStartTime().format(fmt) + " - " + timeSlot.getEndTime().format(fmt));
        appointment.setStatus(AppointmentStatus.BOOKED);
        return appointment;
    }

    public AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        log.debug("Converting Appointment to ResponseDTO for appointmentId: {}", appointment.getAppointmentId());
        return new AppointmentResponseDTO(
            appointment.getAppointmentId(),
            appointment.getPatientId(),
            appointment.getPatientName(),
            appointment.getPatientEmail(),
            appointment.getDoctorId(),
            appointment.getDoctorName(),
            appointment.getTimeSlotId(),
            appointment.getTimeSlotName(),
            appointment.getStatus(),
            appointment.getCreatedAt()
        );
    }

    public AppointmentEventDTO toEventDTO(Appointment appointment, String eventType) {
        log.debug("Converting Appointment to EventDTO for appointmentId: {}, eventType: {}", appointment.getAppointmentId(), eventType);
        AppointmentEventDTO dto = new AppointmentEventDTO();
        dto.setEventType(eventType);
        dto.setAppointmentId(appointment.getAppointmentId());
        dto.setPatientId(appointment.getPatientId());
        dto.setPatientName(appointment.getPatientName());
        dto.setPatientEmail(appointment.getPatientEmail());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setDoctorName(appointment.getDoctorName());
        dto.setTimeSlotId(appointment.getTimeSlotId());
        dto.setStatus(appointment.getStatus().name());
        return dto;
    }
}
