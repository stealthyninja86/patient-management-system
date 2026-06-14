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

    @Column(nullable = false)
    private String externalId;

    public UserProfile() {}

    public UserProfile(User user, ProfileType type, String externalId) {
        this.user = user;
        this.type = type;
        this.externalId = externalId;
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
    public String getExternalId() {
        return externalId;
    }
}
