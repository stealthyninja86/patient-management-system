package com.pms.authservice.service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = Base64.getDecoder()
                .decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String role, Map<String, Object> extraClaims) {
        log.debug("Generating JWT token for email: {} with role: {}", email, role);
        var builder = Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(secretKey);

        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        return builder.compact();
    }

    public String generateToken(String email, String role, String doctorId) {
        log.debug("Generating JWT token for email: {} with role: {} and doctorId: {}", email, role, doctorId);
        return generateToken(email, role, doctorId != null ? Map.of("doctorId", doctorId) : null);
    }

    public String generateToken(String email, String role) {
        log.debug("Generating JWT token for email: {} with role: {} (no extra claims)", email, role);
        return generateToken(email, role, (Map<String, Object>) null);
    }

    public void validateToken(String token) {
        log.debug("Validating JWT token");
        Jwts.parser().verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public String getDoctorIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("doctorId", String.class);
        } catch (Exception e) {
            log.error("Failed to extract doctorId from token: {}", e.getMessage());
            throw new JwtException("Failed to extract doctorId from token", e);
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("Failed to extract role from token: {}", e.getMessage());
            throw new JwtException("Failed to extract role from token", e);
        }
    }
}
