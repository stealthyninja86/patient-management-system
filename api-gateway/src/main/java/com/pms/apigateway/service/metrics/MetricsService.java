package com.pms.apigateway.service.metrics;

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

    public void recordRequest(String routeId, String status) {
        Counter.builder("gateway.requests.total")
                .description("Total requests routed through gateway")
                .tag("routeId", routeId)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void recordLatency(String routeId, long millis) {
        Timer.builder("gateway.request.latency")
                .description("Request latency per route")
                .tag("routeId", routeId)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(millis, TimeUnit.MILLISECONDS);
    }
}
