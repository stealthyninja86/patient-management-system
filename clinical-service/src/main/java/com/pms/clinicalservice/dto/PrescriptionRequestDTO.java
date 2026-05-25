package com.pms.clinicalservice.dto;

import com.pms.clinicalservice.model.DrugType;

import java.util.List;

public record PrescriptionRequestDTO(String patientId, List<DrugInput> drugs, String diagnosis, int painScore, String allergies) {

    public record DrugInput(String name, String dosage, String description, String usage, DrugType type) {}

    public static PrescriptionRequestDTOBuilder builder() {
        return new PrescriptionRequestDTOBuilder();
    }

    public static class PrescriptionRequestDTOBuilder {
        private String patientId;
        private List<DrugInput> drugs;
        private String diagnosis;
        private int painScore;
        private String allergies;

        PrescriptionRequestDTOBuilder() {}

        public PrescriptionRequestDTOBuilder patientId(String patientId) { this.patientId = patientId; return this; }
        public PrescriptionRequestDTOBuilder drugs(List<DrugInput> drugs) { this.drugs = drugs; return this; }
        public PrescriptionRequestDTOBuilder diagnosis(String diagnosis) { this.diagnosis = diagnosis; return this; }
        public PrescriptionRequestDTOBuilder painScore(int painScore) { this.painScore = painScore; return this; }
        public PrescriptionRequestDTOBuilder allergies(String allergies) { this.allergies = allergies; return this; }

        public PrescriptionRequestDTO build() {
            return new PrescriptionRequestDTO(patientId, drugs, diagnosis, painScore, allergies);
        }
    }
}
