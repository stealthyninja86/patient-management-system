package com.pms.patient_service.strategy.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccessStrategyProvider {

    private static final Logger log = LoggerFactory.getLogger(AccessStrategyProvider.class);

    private final List<AccessStrategy> strategies;

    public AccessStrategyProvider(List<AccessStrategy> strategies) {
        this.strategies = strategies;
    }

    public AccessStrategy getStrategy(String role) {
        log.debug("Getting access strategy for role: {}", role);
        return strategies.stream()
                .filter(s -> s.getClass().getSimpleName()
                        .replace("AccessStrategy", "")
                        .equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + role));
    }
}
