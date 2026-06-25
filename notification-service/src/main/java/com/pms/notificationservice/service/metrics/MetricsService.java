package com.pms.notificationservice.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter otpGenerated;
    private final Counter otpVerified;
    private final Counter otpFailed;
    private final Counter dedupHit;
    private final Counter dedupMiss;
    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.otpGenerated = Counter.builder("otp.generated")
                .description("OTP codes generated")
                .register(meterRegistry);
        this.otpVerified = Counter.builder("otp.verified")
                .description("OTP codes verified")
                .register(meterRegistry);
        this.otpFailed = Counter.builder("otp.failed")
                .description("OTP verification failures")
                .register(meterRegistry);
        this.dedupHit = Counter.builder("redis.dedup.hit")
                .description("Duplicate notification suppressed via Redis SETNX")
                .register(meterRegistry);
        this.dedupMiss = Counter.builder("redis.dedup.miss")
                .description("Notification proceeding past dedup check")
                .register(meterRegistry);
    }

    public void recordNotificationSent(String channel) {
        Counter.builder("notifications.sent")
                .description("Notifications sent by channel")
                .tag("channel", channel)
                .register(meterRegistry)
                .increment();
    }

    public void recordNotificationFailed(String channel) {
        Counter.builder("notifications.failed")
                .description("Notifications failed after 3 retries by channel")
                .tag("channel", channel)
                .register(meterRegistry)
                .increment();
    }

    public void recordOtpGenerated() { otpGenerated.increment(); }

    public void recordOtpVerified() { otpVerified.increment(); }

    public void recordOtpFailed() { otpFailed.increment(); }

    public void recordDedupHit() { dedupHit.increment(); }

    public void recordDedupMiss() { dedupMiss.increment(); }
}
