package com.pms.hospitalservice.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String departmentId;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @OneToMany(mappedBy = "department", cascade = CascadeType.PERSIST)
    private List<Doctor> doctorList;

    public Department() {}

    public Department(UUID id, String departmentId, String name, Hospital hospital, List<Doctor> doctorList) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
        this.hospital = hospital;
        this.doctorList = doctorList;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Hospital getHospital() {
        return hospital;
    }
    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }
    public List<Doctor> getDoctorList() {
        return doctorList;
    }
    public void setDoctorList(List<Doctor> doctorList) {
        this.doctorList = doctorList;
    }

    public static DepartmentBuilder builder() {
        return new DepartmentBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", departmentId='" + departmentId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public static class DepartmentBuilder {
        private UUID id;
        private String departmentId;
        private String name;
        private Hospital hospital;
        private List<Doctor> doctorList;

        DepartmentBuilder() {}

        public DepartmentBuilder id(UUID id) {
            this.id = id;
            return this;
        }
        public DepartmentBuilder departmentId(String departmentId) {
            this.departmentId = departmentId;
            return this;
        }
        public DepartmentBuilder name(String name) {
            this.name = name;
            return this;
        }
        public DepartmentBuilder hospital(Hospital hospital) {
            this.hospital = hospital;
            return this;
        }
        public DepartmentBuilder doctorList(List<Doctor> doctorList) {
            this.doctorList = doctorList;
            return this;
        }

        public Department build() {
            return new Department(id, departmentId, name, hospital, doctorList);
        }
    }
}
