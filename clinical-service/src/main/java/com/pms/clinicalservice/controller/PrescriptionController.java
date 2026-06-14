package com.pms.clinicalservice.controller;

import com.pms.clinicalservice.dto.DoctorContactUpdateDTO;
import com.pms.clinicalservice.dto.HospitalContactUpdateDTO;
import com.pms.clinicalservice.dto.PatientContactUpdateDTO;
import com.pms.clinicalservice.dto.PrescriptionRequestDTO;
import com.pms.clinicalservice.dto.PrescriptionResponseDTO;
import com.pms.clinicalservice.service.facade.PrescriptionFacade;
import com.pms.clinicalservice.service.PrescriptionService;
import com.pms.clinicalservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {

    private final PrescriptionFacade prescriptionFacade;
    private final PrescriptionService prescriptionService;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);

    public PrescriptionController(PrescriptionFacade prescriptionFacade,
                                  PrescriptionService prescriptionService,
                                  JwtUtil jwtUtil) {
        this.prescriptionFacade = prescriptionFacade;
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

    @GetMapping("/search")
    public ResponseEntity<List<PrescriptionResponseDTO>> searchPrescriptions(
            @RequestParam(required = false) String hospitalId,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.info("GET /prescriptions/search?hospitalId={}&doctorId={}&startDate={}&endDate={}",
                hospitalId, doctorId, startDate, endDate);
        List<PrescriptionResponseDTO> responses = prescriptionService.searchPrescriptions(
                hospitalId, doctorId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<PrescriptionResponseDTO> createPrescription(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PrescriptionRequestDTO request) {

        String token = authHeader.replace("Bearer ", "");
        String doctorId = jwtUtil.getDoctorIdFromToken(token);

        logger.info("Creating prescription for patient {} by doctor {}", request.patientId(), doctorId);
        PrescriptionResponseDTO response = prescriptionFacade.createPrescription(request, doctorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrescription(@PathVariable String id) {
        logger.info("DELETE /prescriptions/{}", id);
        prescriptionService.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/contact/patient")
    public ResponseEntity<PrescriptionResponseDTO> updatePatientContact(
            @PathVariable String id,
            @RequestBody PatientContactUpdateDTO update) {
        logger.info("PATCH /prescriptions/{}/contact/patient", id);
        PrescriptionResponseDTO response = prescriptionService.updatePatientContact(id, update);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/contact/hospital")
    public ResponseEntity<PrescriptionResponseDTO> updateHospitalContact(
            @PathVariable String id,
            @RequestBody HospitalContactUpdateDTO update) {
        logger.info("PATCH /prescriptions/{}/contact/hospital", id);
        PrescriptionResponseDTO response = prescriptionService.updateHospitalContact(id, update);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/contact/doctor")
    public ResponseEntity<PrescriptionResponseDTO> updateDoctorContact(
            @PathVariable String id,
            @RequestBody DoctorContactUpdateDTO update) {
        logger.info("PATCH /prescriptions/{}/contact/doctor", id);
        PrescriptionResponseDTO response = prescriptionService.updateDoctorContact(id, update);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pdf")
    public ResponseEntity<?> generatePrescriptionPdf(@PathVariable String id) {
        logger.info("Enqueuing PDF generation for id: {}", id);
        prescriptionFacade.enqueuePdfGeneration(id);
        return ResponseEntity.accepted()
                .header("Location", "/prescriptions/" + id + "/pdf")
                .header("Retry-After", "5")
                .build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> getPrescriptionPdf(@PathVariable String id) {
        logger.info("Getting prescription pdf for id: {}", id);
        return prescriptionFacade.getPrescriptionPdf(id);
    }
}
