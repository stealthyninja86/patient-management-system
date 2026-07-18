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

    @JsonProperty("patientEmail")
    private String patientEmail;

    @JsonProperty("patientName")
    private String patientName;

    @JsonProperty("doctorName")
    private String doctorName;

    @JsonProperty("hospitalName")
    private String hospitalName;

    public PrescriptionPdfGeneratedEvent() {}

    public PrescriptionPdfGeneratedEvent(String prescriptionId, String patientId, String patientName,
                                          String patientEmail, String doctorId, String doctorName,
                                          String hospitalId, String hospitalName, String status) {
        this.eventId = UUID.randomUUID().toString();
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.status = status;
        this.generatedAt = Instant.now();
        this.eventType = "PRESCRIPTION_PDF_GENERATED";
        this.version = "1.0";
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
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
