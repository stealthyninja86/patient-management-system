package com.pms.authservice.service;

import com.pms.authservice.model.User;
import com.pms.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        logger.debug("User found: {}", user.isPresent());
        return user;
    }

    public User createUser(User user) {
        logger.debug("Creating user: {}", user.getEmail());
        return userRepository.save(user);
    }
}
