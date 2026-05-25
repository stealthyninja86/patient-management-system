package com.pms.authservice.factory;

import com.pms.authservice.dto.RegisterRequestDTO;
import com.pms.authservice.model.ProfileType;
import com.pms.authservice.model.Role;
import com.pms.authservice.model.User;
import com.pms.authservice.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    private static final Logger log = LoggerFactory.getLogger(UserFactory.class);

    public User createUser(RegisterRequestDTO request, String encodedPassword) {
        log.debug("Creating user: {}", request.email());
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setRole(determineRole(request.role()));
        return user;
    }

    public UserProfile createProfile(User user, ProfileType type, String externalId) {
        log.debug("Creating profile: {} for user: {}", type, user.getEmail());
        UserProfile profile = new UserProfile(user, type, externalId);
        user.addProfile(profile);
        return profile;
    }

    private Role determineRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Role.PATIENT;
        }
    }
}
