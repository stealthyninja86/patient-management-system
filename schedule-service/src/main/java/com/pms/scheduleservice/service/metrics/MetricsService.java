package com.pms.scheduleservice.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final Counter appointmentCancelled;
    private final Timer bookingLatency;
    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.appointmentCancelled = Counter.builder("appointment.cancelled")
                .description("Appointments cancelled")
                .register(meterRegistry);
        this.bookingLatency = Timer.builder("appointment.booking.latency")
                .description("Booking request duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public void recordAppointmentBooked(String status) {
        Counter.builder("appointment.booked")
                .description("Appointments booked by status")
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    public void recordAppointmentCancelled() {
        appointmentCancelled.increment();
    }

    public void recordBookingLatency(long millis) {
        bookingLatency.record(millis, TimeUnit.MILLISECONDS);
    }
}
