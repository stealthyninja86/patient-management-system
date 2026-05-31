package com.pms.clinicalservice.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class PrescriptionPdfGeneratedEvent {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("prescriptionId")
    private String prescriptionId;

    @JsonProperty("patientId")
    private String patientId;

    @JsonProperty("doctorId")
    private String doctorId;

    @JsonProperty("hospitalId")
    private String hospitalId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("generatedAt")
    private Instant generatedAt;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("version")
    private String version;

    public PrescriptionPdfGeneratedEvent() {}

    public PrescriptionPdfGeneratedEvent(String prescriptionId, String patientId,
                                          String doctorId, String hospitalId, String status) {
        this.eventId = UUID.randomUUID().toString();
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.hospitalId = hospitalId;
        this.status = status;
        this.generatedAt = Instant.now();
        this.eventType = "PRESCRIPTION_PDF_GENERATED";
        this.version = "1.0";
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
