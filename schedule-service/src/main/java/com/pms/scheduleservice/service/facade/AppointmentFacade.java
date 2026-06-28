package com.pms.scheduleservice.service.facade;

import com.pms.scheduleservice.dto.request.AppointmentRequestDTO;
import com.pms.scheduleservice.dto.response.AppointmentResponseDTO;
import com.pms.scheduleservice.dto.response.DoctorPatientDTO;
import com.pms.scheduleservice.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppointmentFacade {

    private static final Logger log = LoggerFactory.getLogger(AppointmentFacade.class);

    private final AppointmentService appointmentService;

    public AppointmentFacade(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public List<AppointmentResponseDTO> getAllAppointments() {
        log.debug("Fetching all appointments via facade");
        return appointmentService.getAllAppointments();
    }

    public AppointmentResponseDTO getAppointmentById(String appointmentId) {
        log.debug("Fetching appointment by id via facade: {}", appointmentId);
        return appointmentService.getAppointmentById(appointmentId);
    }

    public List<AppointmentResponseDTO> getAppointmentsByPatient(String patientId) {
        log.debug("Fetching appointments by patient via facade: {}", patientId);
        return appointmentService.getAppointmentsByPatient(patientId);
    }

    public List<AppointmentResponseDTO> getAppointmentsByDoctor(String doctorId) {
        log.debug("Fetching appointments by doctor via facade: {}", doctorId);
        return appointmentService.getAppointmentsByDoctor(doctorId);
    }

    public List<DoctorPatientDTO> getPatientsByDoctor(String doctorId) {
        log.debug("Fetching patients by doctor via facade: {}", doctorId);
        return appointmentService.getPatientsByDoctor(doctorId);
    }

    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request) {
        log.debug("Creating appointment via facade for patient: {}, timeSlot: {}", request.patientId(), request.timeSlotId());
        return appointmentService.createAppointment(request);
    }

    public AppointmentResponseDTO startAppointment(String appointmentId) {
        log.debug("Starting appointment via facade: {}", appointmentId);
        return appointmentService.startAppointment(appointmentId);
    }

    public AppointmentResponseDTO completeAppointment(String appointmentId) {
        log.debug("Completing appointment via facade: {}", appointmentId);
        return appointmentService.completeAppointment(appointmentId);
    }

    public AppointmentResponseDTO cancelAppointment(String appointmentId) {
        log.debug("Cancelling appointment via facade: {}", appointmentId);
        return appointmentService.cancelAppointment(appointmentId);
    }

    public AppointmentResponseDTO rescheduleAppointment(String appointmentId, String newTimeSlotId) {
        log.debug("Rescheduling appointment via facade: {} to timeSlot: {}", appointmentId, newTimeSlotId);
        return appointmentService.rescheduleAppointment(appointmentId, newTimeSlotId);
    }

    public void clearCancelledAppointment(String appointmentId) {
        log.debug("Clearing cancelled appointment via facade: {}", appointmentId);
        appointmentService.clearCancelledAppointment(appointmentId);
    }

    public void deleteAppointment(String appointmentId) {
        log.debug("Deleting appointment via facade: {}", appointmentId);
        appointmentService.deleteAppointment(appointmentId);
    }
}
