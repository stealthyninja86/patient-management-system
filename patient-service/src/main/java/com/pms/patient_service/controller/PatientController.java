package com.pms.patient_service.controller;

import com.pms.patient_service.dto.PatientRequestDTO;
import com.pms.patient_service.dto.PatientResponseDTO;
import com.pms.patient_service.facade.PatientFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing Patients")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);

    private final PatientFacade patientFacade;

    public PatientController(PatientFacade patientFacade) {
        this.patientFacade = patientFacade;
    }

    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        log.info("REST request to get all patients");
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

    @GetMapping("/by-patient-id")
    @Operation(summary = "Get Patient by patientId")
    public ResponseEntity<PatientResponseDTO> getPatientByPatientId(@RequestParam String patientId) {
        log.info("REST request to get patient by patientId: {}", patientId);
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
