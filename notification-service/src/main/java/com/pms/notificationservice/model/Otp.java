package com.pms.notificationservice.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otps")
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String patientId;

    private String doctorId;

    private String hospitalId;

    private String consentRequestId;

    private String phoneHash;

    @Enumerated(EnumType.STRING)
    private OtpStatus status;

    private int attempts;

    private Instant expiresAt;

    private Instant createdAt;

    private Instant verifiedAt;

    public Otp() {}

    public Otp(UUID id, String patientId, String doctorId, String hospitalId, String consentRequestId,
               String phoneHash, OtpStatus status, int attempts, Instant expiresAt, Instant createdAt,
               Instant verifiedAt) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.hospitalId = hospitalId;
        this.consentRequestId = consentRequestId;
        this.phoneHash = phoneHash;
        this.status = status;
        this.attempts = attempts;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.verifiedAt = verifiedAt;
    }

    public static OtpBuilder builder() {
        return new OtpBuilder();
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getHospitalId() {
        return hospitalId;
    }
    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getConsentRequestId() {
        return consentRequestId;
    }
    public void setConsentRequestId(String consentRequestId) {
        this.consentRequestId = consentRequestId;
    }

    public String getPhoneHash() {
        return phoneHash;
    }
    public void setPhoneHash(String phoneHash) {
        this.phoneHash = phoneHash;
    }

    public OtpStatus getStatus() {
        return status;
    }
    public void setStatus(OtpStatus status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }
    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public static class OtpBuilder {

        private UUID id;
        private String patientId;
        private String doctorId;
        private String hospitalId;
        private String consentRequestId;
        private String phoneHash;
        private OtpStatus status;
        private int attempts;
        private Instant expiresAt;
        private Instant createdAt;
        private Instant verifiedAt;

        OtpBuilder() {}

        public OtpBuilder id(UUID id) {
            this.id = id;
            return this;
        }
        public OtpBuilder patientId(String patientId) {
            this.patientId = patientId;
            return this;
        }
        public OtpBuilder doctorId(String doctorId) {
            this.doctorId = doctorId;
            return this;
        }
        public OtpBuilder hospitalId(String hospitalId) {
            this.hospitalId = hospitalId;
            return this;
        }
        public OtpBuilder consentRequestId(String consentRequestId) {
            this.consentRequestId = consentRequestId;
            return this;
        }
        public OtpBuilder phoneHash(String phoneHash) {
            this.phoneHash = phoneHash;
            return this;
        }
        public OtpBuilder status(OtpStatus status) {
            this.status = status;
            return this;
        }
        public OtpBuilder attempts(int attempts) {
            this.attempts = attempts;
            return this;
        }
        public OtpBuilder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }
        public OtpBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        public OtpBuilder verifiedAt(Instant verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public Otp build() {
            return new Otp(
                    id, patientId, doctorId, hospitalId, consentRequestId, phoneHash,
                    status, attempts, expiresAt, createdAt, verifiedAt
            );
        }
    }
}
