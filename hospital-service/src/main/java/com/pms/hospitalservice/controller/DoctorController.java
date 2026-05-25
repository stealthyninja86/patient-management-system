package com.pms.hospitalservice.controller;

import com.pms.hospitalservice.dto.DoctorRequestDTO;
import com.pms.hospitalservice.dto.DoctorResponseDTO;
import com.pms.hospitalservice.facade.DoctorFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private static final Logger log = LoggerFactory.getLogger(DoctorController.class);

    private final DoctorFacade doctorFacade;

    public DoctorController(DoctorFacade doctorFacade) {
        this.doctorFacade = doctorFacade;
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        log.info("GET /doctors");
        return ResponseEntity.ok(doctorFacade.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable UUID id) {
        log.info("GET /doctors/{}", id);
        return ResponseEntity.ok(doctorFacade.getDoctorById(id));
    }

    @GetMapping("/by-doctor-id")
    public ResponseEntity<DoctorResponseDTO> getDoctorByDoctorId(@RequestParam String doctorId) {
        log.info("GET /doctors/by-doctor-id?doctorId={}", doctorId);
        return ResponseEntity.ok(doctorFacade.getDoctorByDoctorId(doctorId));
    }

    @GetMapping("/by-department")
    public ResponseEntity<List<DoctorResponseDTO>> getDoctorsByDepartment(@RequestParam String departmentId) {
        log.info("GET /doctors/by-department?departmentId={}", departmentId);
        return ResponseEntity.ok(doctorFacade.getDoctorsByDepartment(departmentId));
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(@RequestBody DoctorRequestDTO dto) {
        log.info("POST /doctors");
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorFacade.createDoctor(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(@PathVariable UUID id, @RequestBody DoctorRequestDTO dto) {
        log.info("PUT /doctors/{}", id);
        return ResponseEntity.ok(doctorFacade.updateDoctor(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable UUID id) {
        log.info("DELETE /doctors/{}", id);
        doctorFacade.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
