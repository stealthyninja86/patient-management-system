package com.pms.patientservice.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter patientCreated;
    private final Counter consentRevoked;
    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.patientCreated = Counter.builder("patient.created")
                .description("Number of patients created")
                .register(meterRegistry);
        this.consentRevoked = Counter.builder("patient.consent.revoked")
                .description("Consents revoked")
                .register(meterRegistry);
    }

    public void recordPatientCreated() {
        patientCreated.increment();
    }

    public void recordConsentGranted(String type) {
        Counter.builder("patient.consent.granted")
                .description("Consents granted by type")
                .tag("type", type)
                .register(meterRegistry)
                .increment();
    }

    public void recordConsentRevoked() {
        consentRevoked.increment();
    }
}
