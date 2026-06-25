package com.pms.hospitalservice.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final Counter hospitalCreated;
    private final Timer searchLatency;
    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.hospitalCreated = Counter.builder("hospital.created")
                .description("Number of hospitals created")
                .register(meterRegistry);
        this.searchLatency = Timer.builder("hospital.search.latency")
                .description("Hospital Search latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public void recordDoctorCreated(String departmentId) {
        Counter.builder("doctor.created")
                .description("Doctors created by department")
                .tag("departmentId", departmentId)
                .register(meterRegistry)
                .increment();
    }

    public void recordHospitalCreated(long hospitalId) {
        hospitalCreated.increment();
    }

    public void recordSearchLatency(long millis) {
        searchLatency.record(millis, TimeUnit.MILLISECONDS);
    }
}
