package com.pms.patientservice.controller;

import com.pms.patientservice.dto.request.PatientRequestDTO;
import com.pms.patientservice.dto.response.PatientResponseDTO;
import com.pms.patientservice.model.ConsentStatus;
import com.pms.patientservice.repository.ConsentRepository;
import com.pms.patientservice.service.facade.PatientFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing Patients")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);

    private final PatientFacade patientFacade;
    private final ConsentRepository consentRepository;

    public PatientController(PatientFacade patientFacade, ConsentRepository consentRepository) {
        this.patientFacade = patientFacade;
        this.consentRepository = consentRepository;
    }

    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients(@AuthenticationPrincipal Jwt jwt) {
        log.info("REST request to get all patients");
        String role = jwt.getClaimAsString("role");
        if ("DOCTOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(patientFacade.getAllPatients());
    }

    @PostMapping
    @Operation(summary = "Create Patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@RequestBody PatientRequestDTO dto) {
        log.info("REST request to create patient with email: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(patientFacade.createPatient(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Patient")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id, @RequestBody PatientRequestDTO dto) {
        log.info("REST request to update patient with id: {}", id);
        return ResponseEntity.ok(patientFacade.updatePatient(id, dto));
    }

    @GetMapping("/{patientId}")
    @Operation(summary = "Get Patient by patientId")
    public ResponseEntity<PatientResponseDTO> getPatientByPatientId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String patientId) {
        log.info("REST request to get patient by patientId: {}", patientId);
        String role = jwt.getClaimAsString("role");
        if ("DOCTOR".equals(role)) {
            String hospitalId = jwt.getClaimAsString("hospitalId");
            if (hospitalId == null || !consentRepository.existsByPatientIdAndHospitalIdAndStatus(
                    patientId, hospitalId, ConsentStatus.ACTIVE)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(patientFacade.getPatientByPatientId(patientId));
    }

    @GetMapping("/by-patient-id")
    @Operation(summary = "Get Patient by patientId (query param)")
    public ResponseEntity<PatientResponseDTO> getPatientByPatientIdQuery(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String patientId) {
        log.info("REST request to get patient by patientId: {}", patientId);
        String role = jwt.getClaimAsString("role");
        if ("DOCTOR".equals(role)) {
            String hospitalId = jwt.getClaimAsString("hospitalId");
            if (hospitalId == null || !consentRepository.existsByPatientIdAndHospitalIdAndStatus(
                    patientId, hospitalId, ConsentStatus.ACTIVE)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(patientFacade.getPatientByPatientId(patientId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        log.info("REST request to delete patient with id: {}", id);
        patientFacade.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
