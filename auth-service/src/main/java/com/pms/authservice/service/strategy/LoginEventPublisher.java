package com.pms.authservice.service.strategy;

import com.pms.authservice.dto.event.LoginEvent;
import com.pms.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LoginEventPublisher implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger log = LoggerFactory.getLogger(LoginEventPublisher.class);
    private final UserRepository userRepository;

    public LoginEventPublisher(UserRepository userRepository) {
        this.userRepository = userRepository;
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

            log.info("login event: email={} , role={}, timestamp={}", loginEvent.email(), loginEvent.role(), loginEvent.timestamp());
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
