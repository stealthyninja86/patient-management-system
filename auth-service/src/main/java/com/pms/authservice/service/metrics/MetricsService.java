package com.pms.authservice.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordLoginAttempt(String status, String role) {
        Counter.builder("auth.login.attempts")
                .description("Login attempts by status and role")
                .tag("status", status)
                .tag("role", role)
                .register(meterRegistry)
                .increment();
    }

    public void recordTokenValidation(String status) {
        Counter.builder("auth.token.validated")
                .description("Token validations by status")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void recordRegistration(String role) {
        Counter.builder("auth.registration.completed")
                .description("Registrations completed by role")
                .tag("role", role)
                .register(meterRegistry)
                .increment();
    }
}
