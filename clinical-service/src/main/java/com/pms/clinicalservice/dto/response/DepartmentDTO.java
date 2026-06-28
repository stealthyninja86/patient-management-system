package com.pms.clinicalservice.dto.response;

public record DepartmentDTO(String departmentId, String name, String hospitalId, String hospitalName) {

    public static DepartmentDTOBuilder builder() {
        return new DepartmentDTOBuilder();
    }

    public static class DepartmentDTOBuilder {
        private String departmentId;
        private String name;
        private String hospitalId;
        private String hospitalName;

        DepartmentDTOBuilder() {}

        public DepartmentDTOBuilder departmentId(String departmentId) {
            this.departmentId = departmentId;
            return this;
        }
        public DepartmentDTOBuilder name(String name) {
            this.name = name;
            return this;
        }
        public DepartmentDTOBuilder hospitalId(String hospitalId) {
            this.hospitalId = hospitalId;
            return this;
        }
        public DepartmentDTOBuilder hospitalName(String hospitalName) {
            this.hospitalName = hospitalName;
            return this;
        }

        public DepartmentDTO build() {
            return new DepartmentDTO(departmentId, name, hospitalId, hospitalName);
        }
    }
}
