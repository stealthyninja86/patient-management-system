package com.pms.authservice.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserProfile> profiles = new ArrayList<>();

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    public List<UserProfile> getProfiles() {
        return profiles;
    }
    public void setProfiles(List<UserProfile> profiles) {
        this.profiles = profiles;
    }

    public void addProfile(UserProfile profile) {
        profiles.add(profile);
    }

    public String getDoctorId() {
        return profiles.stream()
                .filter(p -> p.getType() == ProfileType.DOCTOR)
                .map(UserProfile::getExternalId)
                .findFirst().orElse(null);
    }

    public String getPatientId() {
        return profiles.stream()
                .filter(p -> p.getType() == ProfileType.PATIENT)
                .map(UserProfile::getExternalId)
                .findFirst().orElse(null);
    }
}
