package com.pms.clinicalservice.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordPrescriptionCreated(String doctorId) {
        Counter.builder("clinical.prescription.created")
                .description("Number of prescriptions created")
                .tag("doctorId", doctorId)
                .register(meterRegistry)
                .increment();
    }

    public void recordPDFGeneration(long millis, String status) {
        Timer.builder("clinical.pdf.generation.duration")
                .description("Time taken for PDF generation")
                .tag("status", status)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(millis, TimeUnit.MILLISECONDS);
    }

    public void recordGrpcTimer(String target, long millis) {
        Timer.builder("clinical.grpc.call.latency")
                .description("gRPC call latency")
                .tag("target", target)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(millis, TimeUnit.MILLISECONDS);
    }
}
