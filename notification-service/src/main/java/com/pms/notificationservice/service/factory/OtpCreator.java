package com.pms.notificationservice.service.factory;

import com.pms.notificationservice.model.Otp;
import com.pms.notificationservice.model.OtpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Component
public class OtpCreator {

    public Otp createOtp(String domainKey, String phoneNumber, Instant expiresAt) {
        return Otp.builder()
                .domainKey(domainKey)
                .phoneHash(hashPhone(phoneNumber))
                .status(OtpStatus.GENERATED)
                .attempts(0)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .build();
    }

    private String hashPhone(String phone) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(
                    phone.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
