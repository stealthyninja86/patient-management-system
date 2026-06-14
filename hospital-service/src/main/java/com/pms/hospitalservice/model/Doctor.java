package com.pms.hospitalservice.model;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String doctorId;

    private String name;

    private String email;

    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    public Doctor() {}

    public Doctor(UUID id, String doctorId, String name, String email, String phone, Department department) {
        this.id = id;
        this.doctorId = doctorId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public static DoctorBuilder builder() {
        return new DoctorBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(id, doctor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", doctorId='" + doctorId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public static class DoctorBuilder {
        private UUID id;
        private String doctorId;
        private String name;
        private String email;
        private String phone;
        private Department department;

        DoctorBuilder() {}

        public DoctorBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public DoctorBuilder doctorId(String doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public DoctorBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DoctorBuilder email(String email) {
            this.email = email;
            return this;
        }

        public DoctorBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public DoctorBuilder department(Department department) {
            this.department = department;
            return this;
        }

        public Doctor build() {
            return new Doctor(id, doctorId, name, email, phone, department);
        }
    }
}
