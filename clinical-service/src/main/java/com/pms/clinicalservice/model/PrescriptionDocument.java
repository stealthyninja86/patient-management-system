package com.pms.clinicalservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class PrescriptionDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String prescriptionId;

    private String fileName;

    private LocalDateTime generatedAt;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    public PrescriptionDocument() {}

    public PrescriptionDocument(UUID id, String prescriptionId, String fileName,
                                LocalDateTime generatedAt, DocumentStatus status) {
        this.id = id;
        this.prescriptionId = prescriptionId;
        this.fileName = fileName;
        this.generatedAt = generatedAt;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public static PrescriptionDocumentBuilder builder() {
        return new PrescriptionDocumentBuilder();
    }

    public static class PrescriptionDocumentBuilder {
        private UUID id;
        private String prescriptionId;
        private String fileName;
        private LocalDateTime generatedAt;
        private DocumentStatus status;

        PrescriptionDocumentBuilder() {}

        public PrescriptionDocumentBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public PrescriptionDocumentBuilder prescriptionId(String prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }

        public PrescriptionDocumentBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public PrescriptionDocumentBuilder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public PrescriptionDocumentBuilder status(DocumentStatus status) {
            this.status = status;
            return this;
        }

        public PrescriptionDocument build() {
            return new PrescriptionDocument(id, prescriptionId, fileName, generatedAt, status);
        }
    }
}
