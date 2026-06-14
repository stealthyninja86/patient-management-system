package com.pms.scheduleservice.controller;

import com.pms.scheduleservice.dto.TimeSlotRequestDTO;
import com.pms.scheduleservice.dto.TimeSlotResponseDTO;
import com.pms.scheduleservice.service.facade.TimeSlotFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/time-slots")
public class TimeSlotController {

    private static final Logger log = LoggerFactory.getLogger(TimeSlotController.class);

    private final TimeSlotFacade timeSlotFacade;

    public TimeSlotController(TimeSlotFacade timeSlotFacade) {
        this.timeSlotFacade = timeSlotFacade;
    }

    @GetMapping
    public ResponseEntity<List<TimeSlotResponseDTO>> getAllTimeSlots() {
        log.info("REST request to get all time slots");
        return ResponseEntity.ok(timeSlotFacade.getAllTimeSlots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeSlotResponseDTO> getTimeSlotById(@PathVariable String id) {
        log.info("REST request to get time slot by id: {}", id);
        return ResponseEntity.ok(timeSlotFacade.getTimeSlotById(id));
    }

    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<TimeSlotResponseDTO>> getTimeSlotsByDoctor(@PathVariable String doctorId) {
        log.info("REST request to get time slots by doctor: {}", doctorId);
        return ResponseEntity.ok(timeSlotFacade.getTimeSlotsByDoctor(doctorId));
    }

    @GetMapping("/by-doctor/{doctorId}/available")
    public ResponseEntity<List<TimeSlotResponseDTO>> getAvailableTimeSlotsByDoctor(@PathVariable String doctorId) {
        log.info("REST request to get available time slots by doctor: {}", doctorId);
        return ResponseEntity.ok(timeSlotFacade.getAvailableTimeSlotsByDoctor(doctorId));
    }

    @PostMapping
    public ResponseEntity<TimeSlotResponseDTO> createTimeSlot(@RequestBody TimeSlotRequestDTO request) {
        log.info("REST request to create time slot for doctor: {}", request.doctorId());
        TimeSlotResponseDTO response = timeSlotFacade.createTimeSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable UUID id) {
        log.info("REST request to delete time slot: {}", id);
        timeSlotFacade.deleteTimeSlot(id);
        return ResponseEntity.noContent().build();
    }
}
