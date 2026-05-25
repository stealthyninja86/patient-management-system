package com.pms.clinicalservice.dto;

public record DoctorDTO(String doctorId, String name, String departmentId) {

    public static DoctorDTOBuilder builder() {
        return new DoctorDTOBuilder();
    }

    public static class DoctorDTOBuilder {
        private String doctorId;
        private String name;
        private String departmentId;

        DoctorDTOBuilder() {}

        public DoctorDTOBuilder doctorId(String doctorId) { this.doctorId = doctorId; return this; }
        public DoctorDTOBuilder name(String name) { this.name = name; return this; }
        public DoctorDTOBuilder departmentId(String departmentId) { this.departmentId = departmentId; return this; }

        public DoctorDTO build() {
            return new DoctorDTO(doctorId, name, departmentId);
        }
    }
}
