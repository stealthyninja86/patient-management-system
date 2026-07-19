package com.pms.scheduleservice.controller;

import com.pms.scheduleservice.dto.request.AppointmentRequestDTO;
import com.pms.scheduleservice.dto.request.ConfirmAppointmentRequestDTO;
import com.pms.scheduleservice.dto.response.AppointmentResponseDTO;
import com.pms.scheduleservice.dto.response.DoctorPatientDTO;
import com.pms.scheduleservice.dto.request.RescheduleRequestDTO;
import com.pms.scheduleservice.model.AppointmentStatus;
import com.pms.scheduleservice.service.facade.AppointmentFacade;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentFacade appointmentFacade;

    public AppointmentController(AppointmentFacade appointmentFacade) {
        this.appointmentFacade = appointmentFacade;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments() {
        log.info("REST request to get all appointments");
        return ResponseEntity.ok(appointmentFacade.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable String id) {
        log.info("REST request to get appointment by id: {}", id);
        return ResponseEntity.ok(appointmentFacade.getAppointmentById(id));
    }

    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByPatient(@PathVariable String patientId) {
        log.info("REST request to get appointments by patient: {}", patientId);
        return ResponseEntity.ok(appointmentFacade.getAppointmentsByPatient(patientId));
    }

    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByDoctor(@PathVariable String doctorId) {
        log.info("REST request to get appointments by doctor: {}", doctorId);
        return ResponseEntity.ok(appointmentFacade.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping("/by-doctor/{doctorId}/patients")
    public ResponseEntity<List<DoctorPatientDTO>> getPatientsByDoctor(@PathVariable String doctorId) {
        log.info("REST request to get patients by doctor: {}", doctorId);
        return ResponseEntity.ok(appointmentFacade.getPatientsByDoctor(doctorId));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(@RequestBody @Valid AppointmentRequestDTO request) {
        log.info("REST request to create appointment for patient: {}, timeSlot: {}", request.patientId(), request.timeSlotId());
        String appointmentId = appointmentFacade.initBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("appointmentId", appointmentId, "message", "OTP sent to registered phone/email")
        );
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startAppointment(@PathVariable String id) {
        log.info("REST request to initiate start for appointment: {}", id);
        appointmentFacade.initStart(id);
        return ResponseEntity.ok(Map.of("message", "OTP sent to registered phone/email"));
    }

    @PostMapping("/{id}/start/confirm")
    public ResponseEntity<AppointmentResponseDTO> confirmStart(
            @PathVariable String id,
            @RequestBody @Valid ConfirmAppointmentRequestDTO request) {
        log.info("REST request to confirm start for appointment: {}", id);
        AppointmentResponseDTO result = appointmentFacade.confirmStart(id, request.code());
        if (result.status() == AppointmentStatus.CANCELLED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(@PathVariable String id) {
        log.info("REST request to complete appointment: {}", id);
        return ResponseEntity.ok(appointmentFacade.completeAppointment(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(@PathVariable String id) {
        log.info("REST request to cancel appointment: {}", id);
        return ResponseEntity.ok(appointmentFacade.cancelAppointment(id));
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(@PathVariable String id,
                                                                         @RequestBody RescheduleRequestDTO request) {
        log.info("REST request to reschedule appointment: {} to timeSlot: {}", id, request.newTimeSlotId());
        return ResponseEntity.ok(appointmentFacade.rescheduleAppointment(id, request.newTimeSlotId()));
    }

    @PostMapping("/{id}/clear-cancelled")
    public ResponseEntity<Void> clearCancelledAppointment(@PathVariable String id) {
        log.info("REST request to clear cancelled appointment: {}", id);
        appointmentFacade.clearCancelledAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String id) {
        log.info("REST request to delete appointment: {}", id);
        appointmentFacade.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointment(
            @PathVariable String id,
            @RequestBody @Valid ConfirmAppointmentRequestDTO request) {
        AppointmentResponseDTO result = appointmentFacade.confirmBooking(id, request.code());
        if (result.status() == AppointmentStatus.CANCELLED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/confirm-dev")
    public ResponseEntity<AppointmentResponseDTO> confirmAppointmentDev(@PathVariable String id) {
        log.info("Dev bypass: confirming appointment {} without OTP", id);
        return ResponseEntity.ok(appointmentFacade.confirmBookingDev(id));
    }

    @PostMapping("/{id}/start-dev")
    public ResponseEntity<AppointmentResponseDTO> startAppointmentDev(@PathVariable String id) {
        log.info("Dev bypass: starting appointment {} without OTP", id);
        return ResponseEntity.ok(appointmentFacade.confirmStartDev(id));
    }
}
