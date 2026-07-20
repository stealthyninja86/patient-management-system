package com.pms.authservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileType type;

    @Column(nullable = true)
    private String doctorId;

    @Column(nullable = true)
    private String patientId;

    @Column(nullable = true)
    private String hospitalId;

    public UserProfile() {}

    public UserProfile(User user, ProfileType type, String doctorId, String patientId, String hospitalId) {
        this.user = user;
        this.type = type;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.hospitalId = hospitalId;
    }

    public Long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public ProfileType getType() {
        return type;
    }
    public String getDoctorId() {
        return doctorId;
    }
    public String getPatientId() {
        return patientId;
    }
    public String getHospitalId() {
        return hospitalId;
    }
}
