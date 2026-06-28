package com.pms.authservice.controller;

import com.pms.authservice.dto.request.AdminRegisterRequestDTO;
import com.pms.authservice.dto.request.DoctorRegisterRequestDTO;
import com.pms.authservice.dto.request.PatientRegisterRequestDTO;
import com.pms.authservice.dto.response.AdminRegisterResponseDTO;
import com.pms.authservice.dto.response.DepartmentDTO;
import com.pms.authservice.dto.response.DoctorRegisterResponseDTO;
import com.pms.authservice.dto.response.HospitalDTO;
import com.pms.authservice.dto.response.PatientRegisterResponseDTO;
import com.pms.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Get all hospitals")
    @GetMapping("/hospitals")
    public ResponseEntity<List<HospitalDTO>> getHospitals() {
        logger.info("Fetching hospitals");
        return ResponseEntity.ok(authService.getAllHospitals());
    }

    @Operation(summary = "Get departments by hospital")
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getDepartments(
            @RequestParam(value = "hospitalId", required = false) String hospitalId) {
        logger.info("Fetching departments for hospitalId: {}", hospitalId);
        return ResponseEntity.ok(authService.getAllDepartments(hospitalId));
    }

    @Operation(summary = "Register a new admin")
    @PostMapping("/register/admin")
    public ResponseEntity<AdminRegisterResponseDTO> registerAdmin(@Valid @RequestBody AdminRegisterRequestDTO request) {
        logger.info("Admin registration request for email: {}", request.email());
        return ResponseEntity.ok(authService.registerAdmin(request));
    }

    @Operation(summary = "Register a new doctor")
    @PostMapping("/register/doctor")
    public ResponseEntity<DoctorRegisterResponseDTO> registerDoctor(@Valid @RequestBody DoctorRegisterRequestDTO request) {
        logger.info("Doctor registration request for email: {}", request.email());
        return ResponseEntity.ok(authService.registerDoctor(request));
    }

    @Operation(summary = "Register a new patient")
    @PostMapping("/register/patient")
    public ResponseEntity<PatientRegisterResponseDTO> registerPatient(@Valid @RequestBody PatientRegisterRequestDTO request) {
        logger.info("Patient registration request for email: {}", request.email());
        return ResponseEntity.ok(authService.registerPatient(request));
    }
}
