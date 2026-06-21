package com.pms.patientservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String patientId;

    @NotNull
    private String name;

    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    @NotNull
    private String address;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private LocalDate registeredDate;

    private String gender;

    private String phone;

    private String bloodType;

    public Patient() {}

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
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

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }
    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBloodType() {
        return bloodType;
    }
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String patientId;
        private String name;
        private String email;
        private String address;
        private LocalDate dateOfBirth;
        private LocalDate registeredDate;
        private String gender;
        private String phone;
        private String bloodType;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }
        public Builder patientId(String patientId) {
            this.patientId = patientId;
            return this;
        }
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        public Builder address(String address) {
            this.address = address;
            return this;
        }
        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }
        public Builder registeredDate(LocalDate registeredDate) {
            this.registeredDate = registeredDate;
            return this;
        }
        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        public Builder bloodType(String bloodType) {
            this.bloodType = bloodType;
            return this;
        }

        public Patient build() {
            Patient p = new Patient();
            p.id = this.id;
            p.patientId = this.patientId;
            p.name = this.name;
            p.email = this.email;
            p.address = this.address;
            p.dateOfBirth = this.dateOfBirth;
            p.registeredDate = this.registeredDate;
            p.gender = this.gender;
            p.phone = this.phone;
            p.bloodType = this.bloodType;
            return p;
        }
    }
}
