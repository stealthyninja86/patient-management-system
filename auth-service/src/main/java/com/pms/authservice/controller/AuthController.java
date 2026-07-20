package com.pms.authservice.controller;

import com.pms.authservice.dto.request.AdminRegisterRequestDTO;
import com.pms.authservice.dto.request.DoctorRegisterRequestDTO;
import com.pms.authservice.dto.request.PatientRegisterRequestDTO;
import com.pms.authservice.dto.response.AdminRegisterResponseDTO;
import com.pms.authservice.dto.response.DepartmentDTO;

import com.pms.authservice.dto.response.HospitalDTO;
import com.pms.authservice.dto.response.PatientRegisterResponseDTO;
import com.pms.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final JwtDecoder jwtDecoder;

    public AuthController(AuthService authService, JwtDecoder jwtDecoder) {
        this.authService = authService;
        this.jwtDecoder = jwtDecoder;
    }

    @Operation(summary = "Generate JWT token")
    @PostMapping("/token")
    public ResponseEntity<?> generateToken(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required"));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "password is required"));
        }

        Map<String, String> tokenData = authService.generateToken(email, password);
        if (tokenData == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        return ResponseEntity.ok(tokenData);
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

    @Operation(summary = "Register a new doctor (admin only)")
    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody DoctorRegisterRequestDTO request) {
        logger.info("Admin doctor registration request for email: {}", request.email());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing token"));
        }
        try {
            Jwt jwt = jwtDecoder.decode(authHeader.substring(7));
            String role = jwt.getClaimAsString("role");
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Admin role required"));
            }
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }

        return ResponseEntity.ok(authService.registerDoctor(request));
    }

    @Operation(summary = "Register a new patient")
    @PostMapping("/register/patient")
    public ResponseEntity<PatientRegisterResponseDTO> registerPatient(@Valid @RequestBody PatientRegisterRequestDTO request) {
        logger.info("Patient registration request for email: {}", request.email());
        return ResponseEntity.ok(authService.registerPatient(request));
    }
}
