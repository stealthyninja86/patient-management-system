package com.pms.patientservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String consentRequestId;

    private String patientId;
    private String doctorId;
    private String hospitalId;

    @Enumerated(EnumType.STRING)
    private ConsentStatus status;

    private LocalDateTime createdAt;

    public Consent() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ConsentStatus.PENDING_OTP;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getConsentRequestId() { return consentRequestId; }
    public void setConsentRequestId(String consentRequestId) { this.consentRequestId = consentRequestId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public ConsentStatus getStatus() { return status; }
    public void setStatus(ConsentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
