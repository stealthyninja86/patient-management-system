package com.pms.scheduleservice.controller;

import com.pms.scheduleservice.dto.request.AppointmentRequestDTO;
import com.pms.scheduleservice.dto.response.AppointmentResponseDTO;
import com.pms.scheduleservice.dto.response.DoctorPatientDTO;
import com.pms.scheduleservice.dto.request.RescheduleRequestDTO;
import com.pms.scheduleservice.service.facade.AppointmentFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@RequestBody AppointmentRequestDTO request) {
        log.info("REST request to create appointment for patient: {}, timeSlot: {}", request.patientId(), request.timeSlotId());
        AppointmentResponseDTO response = appointmentFacade.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<AppointmentResponseDTO> startAppointment(@PathVariable String id) {
        log.info("REST request to start appointment: {}", id);
        return ResponseEntity.ok(appointmentFacade.startAppointment(id));
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
}
