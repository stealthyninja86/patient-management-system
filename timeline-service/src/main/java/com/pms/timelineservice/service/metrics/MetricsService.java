package com.pms.timelineservice.service.metrics;

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

    public void recordEventConsumed(String topic) {
        Counter.builder("analytics.events.consumed")
                .description("Kafka events consumed by topic")
                .tag("topic", topic)
                .register(meterRegistry)
                .increment();
    }

    public void recordEventDeduped(String topic) {
        Counter.builder("analytics.events.deduped")
                .description("Duplicate events skipped by topic")
                .tag("topic", topic)
                .register(meterRegistry)
                .increment();
    }

    public void recordAggregationDuration(String type, long millis) {
        Timer.builder("analytics.aggregation.duration")
                .description("Aggregation pipeline duration by type")
                .tag("type", type)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(millis, TimeUnit.MILLISECONDS);
    }
}
