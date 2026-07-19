package com.pms.authservice.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.authservice.dto.event.LoginEvent;
import com.pms.authservice.model.OutboxEvent;
import com.pms.authservice.repository.OutboxRepository;
import com.pms.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class LoginEventPublisher implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger log = LoggerFactory.getLogger(LoginEventPublisher.class);
    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public LoginEventPublisher(UserRepository userRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            log.debug("skipping LoginEventPublisher for non-UserDetails principal: {}",
                principal.getClass().getSimpleName());
            return;
        }

        String username = userDetails.getUsername();

        userRepository.findByEmail(username).ifPresent(user -> {
            LoginEvent loginEvent = new LoginEvent(
                    user.getEmail(),
                    user.getRole().name(),
                    Instant.now(),
                    getClientIP(event)
            );

            log.info("Login event: email={}, role={}, timestamp={}", loginEvent.email(), loginEvent.role(), loginEvent.timestamp());

            try {
                String payload = objectMapper.writeValueAsString(loginEvent);
                OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.randomUUID(),
                    "USER_LOGIN",
                    user.getEmail(),
                    "LOGIN_EVENT",
                    "user-login",
                    payload,
                    user.getEmail(),
                    false,
                    LocalDateTime.now(),
                    null
                );
                outboxRepository.save(outboxEvent);
                log.debug("Login outbox event saved for {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to write login outbox event", e);
            }
        });
    }

    private String getClientIP(AuthenticationSuccessEvent event) {
        Object details = event.getAuthentication().getDetails();
        if(details instanceof WebAuthenticationDetails) {
            return ((WebAuthenticationDetails) details).getRemoteAddress();
        }
        return "unknown";
    }
}
