package com.pms.authservice.service.adapter;

import com.pms.authservice.service.strategy.UserLookupStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DelegatingUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(DelegatingUserDetailsService.class);

    private final List<UserLookupStrategy> strategies;

    public DelegatingUserDetailsService(List<UserLookupStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return strategies.stream()
                .filter(strategy -> {
                    boolean supported = strategy.supports(identifier);
                    log.trace("Strategy {} supports '{}': {}",
                            strategy.getClass().getSimpleName(), identifier, supported);
                    return supported;
                })
                .findFirst()
                .map(strategy -> {
                    log.debug("Strategy {} finds '{}'", strategy.getClass().getSimpleName(), identifier);
                    return strategy.loadUser(identifier);
                })
                .orElseThrow(() -> {
                    log.warn("Strategy {} not found", identifier);
                    return new UsernameNotFoundException(identifier);
                });
    }
}
