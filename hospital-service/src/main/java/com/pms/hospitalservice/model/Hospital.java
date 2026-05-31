package com.pms.hospitalservice.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String hospitalId;

    private String name;

    private String address;

    private String email;

    private String phone;

    private String website;

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Department> departmentList;

    public Hospital() {}

    public Hospital(UUID id, String hospitalId, String name, String address, List<Department> departmentList, String website, String email, String phone) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.name = name;
        this.address = address;
        this.departmentList = departmentList;
        this.website = website;
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Department> getDepartmentList() {
        return departmentList;
    }

    public void setDepartmentList(List<Department> departmentList) {
        this.departmentList = departmentList;
    }

    public static HospitalBuilder builder() {
        return new HospitalBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hospital hospital = (Hospital) o;
        return Objects.equals(id, hospital.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Hospital{" +
                "id=" + id +
                ", hospitalId='" + hospitalId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    public static class HospitalBuilder {
        private UUID id;
        private String hospitalId;
        private String name;
        private String address;
        private List<Department> departmentList;
        private String website;
        private String email;
        private String phone;

        HospitalBuilder() {}

        public HospitalBuilder website(String website) {
            this.website = website;
            return this;
        }

        public HospitalBuilder email(String email) {
            this.email = email;
            return this;
        }

        public HospitalBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public HospitalBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public HospitalBuilder hospitalId(String hospitalId) {
            this.hospitalId = hospitalId;
            return this;
        }

        public HospitalBuilder name(String name) {
            this.name = name;
            return this;
        }

        public HospitalBuilder address(String address) {
            this.address = address;
            return this;
        }

        public HospitalBuilder departmentList(List<Department> departmentList) {
            this.departmentList = departmentList;
            return this;
        }

        public Hospital build() {
            return new Hospital(id, hospitalId, name, address, departmentList, website, email, phone);
        }
    }
}
