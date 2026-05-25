package com.pms.authservice.controller;

import com.pms.authservice.dto.DepartmentDTO;
import com.pms.authservice.dto.HospitalDTO;
import com.pms.authservice.dto.LoginRequestDTO;
import com.pms.authservice.dto.LoginResponseDTO;
import com.pms.authservice.dto.RegisterRequestDTO;
import com.pms.authservice.dto.RegisterResponseDTO;
import com.pms.authservice.exception.InvalidCredentialsException;
import com.pms.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        logger.info("Login attempt for email: {}", loginRequestDTO.getEmail());
        try {
            LoginResponseDTO response = authService.authenticate(loginRequestDTO);
            logger.info("Login successful for email: {}", loginRequestDTO.getEmail());
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialsException e) {
            logger.warn("Login failed for email: {}", loginRequestDTO.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Validate token")
    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
        logger.info("Validate token request");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return authService.validateToken(authHeader.substring(7))
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        logger.info("Registration request for email: {} and role: {}", request.email(), request.role());
        try {
            RegisterResponseDTO response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Registration failed", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
