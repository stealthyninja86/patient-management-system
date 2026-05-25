package com.pms.clinicalservice.controller;

import com.pms.clinicalservice.dto.PrescriptionRequestDTO;
import com.pms.clinicalservice.dto.PrescriptionResponseDTO;
import com.pms.clinicalservice.service.PrescriptionService;
import com.pms.clinicalservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);

    public PrescriptionController(PrescriptionService prescriptionService, JwtUtil jwtUtil) {
        this.prescriptionService = prescriptionService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDTO> getPrescriptionById(@PathVariable String id) {
        logger.info("GET /prescriptions/{}", id);
        PrescriptionResponseDTO response = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponseDTO>> getPrescriptionsByPatientId(@PathVariable String patientId) {
        logger.info("GET /prescriptions/by-patient/{}", patientId);
        List<PrescriptionResponseDTO> responses = prescriptionService.getPrescriptionsByPatientId(patientId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<PrescriptionResponseDTO>> getPrescriptionsByDoctorId(@PathVariable String doctorId) {
        logger.info("GET /prescriptions/by-doctor/{}", doctorId);
        List<PrescriptionResponseDTO> responses = prescriptionService.getPrescriptionsByDoctorId(doctorId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<PrescriptionResponseDTO> createPrescription(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PrescriptionRequestDTO request) {
        String token = authHeader.replace("Bearer ", "");
        String doctorId = jwtUtil.getDoctorIdFromToken(token);
        logger.info("Creating prescription for patient {} by doctor {}", request.patientId(), doctorId);
        PrescriptionResponseDTO response = prescriptionService.createPrescription(request, doctorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrescription(@PathVariable String id) {
        logger.info("DELETE /prescriptions/{}", id);
        prescriptionService.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }
}
