package com.pms.clinicalservice.dto.response;

public record PatientDTO(String patientId, String name, String email, String phone) {

    public static PatientDTOBuilder builder() {
        return new PatientDTOBuilder();
    }

    public static class PatientDTOBuilder {
        private String patientId;
        private String name;
        private String email;
        private String phone;

        PatientDTOBuilder() {}

        public PatientDTOBuilder patientId(String patientId) {
            this.patientId = patientId;
            return this;
        }
        public PatientDTOBuilder name(String name) {
            this.name = name;
            return this;
        }
        public PatientDTOBuilder email(String email) {
            this.email = email;
            return this;
        }
        public PatientDTOBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public PatientDTO build() {
            return new PatientDTO(patientId, name, email, phone);
        }
    }
}
