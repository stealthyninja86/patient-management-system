package com.pms.authservice.controller;

import com.pms.authservice.model.User;
import com.pms.authservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class TokenController {

    private static final Logger log = LoggerFactory.getLogger(TokenController.class);
    private final UserService userService;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;

    public TokenController(UserService userService, JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtEncoder = jwtEncoder;
        this.passwordEncoder = passwordEncoder;
    }

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

        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Failed login attempt for user: {}", email);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("http://auth-service:4005")
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("role", user.getRole().name())
                .claim("userId", user.getId().toString());

        if (user.getDoctorId() != null) {
            claimsBuilder.claim("doctorId", user.getDoctorId());
        }
        if (user.getPatientId() != null) {
            claimsBuilder.claim("patientId", user.getPatientId());
        }

        JwtClaimsSet claims = claimsBuilder.build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        log.info("Generated token for user: {} - role={}", email, user.getRole().name());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", user.getRole().name(),
                "userId", user.getId().toString()
        ));
    }
}
