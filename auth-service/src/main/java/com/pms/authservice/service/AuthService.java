package com.pms.authservice.service;

import com.pms.authservice.dto.LoginRequestDTO;
import com.pms.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
        logger.debug("Authenticating user: {}", loginRequestDTO.getEmail());
        Optional<String> token = userService
                .findByEmail(loginRequestDTO.getEmail())
                .filter(u -> {
                    boolean matches = passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword());
                    logger.debug("Password match result: {}", matches);
                    return matches;
                })
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));

        return token;
    }

    public boolean validateToken(String token) {
        try{
            jwtUtil.validateToken(token);
            return true;
        }catch (JwtException e){
            return false;
        }
    }
}
