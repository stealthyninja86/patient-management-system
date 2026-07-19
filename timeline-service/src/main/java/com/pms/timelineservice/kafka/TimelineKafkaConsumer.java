package com.pms.timelineservice.kafka;

import com.pms.timelineservice.dto.event.AppointmentEvent;
import com.pms.timelineservice.dto.event.ConsentGrantedEvent;
import com.pms.timelineservice.dto.event.PrescriptionPdfGeneratedEvent;
import com.pms.timelineservice.service.TimelineIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RetryableTopic(
    attempts = "4",
    backoff = @Backoff(delay = 2000, multiplier = 2.0),
    dltTopicSuffix = "-dlt"
)
public class TimelineKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TimelineKafkaConsumer.class);

    private final TimelineIngestionService ingestionService;
    private final StringRedisTemplate redis;

    public TimelineKafkaConsumer(TimelineIngestionService ingestionService, StringRedisTemplate redis) {
        this.ingestionService = ingestionService;
        this.redis = redis;
    }

    @KafkaListener(topics = "appointment-events",
            containerFactory = "appointmentKafkaListenerContainerFactory",
            groupId = "timeline-service")
    public void consumeAppointmentEvent(AppointmentEvent event) {
        ingestionService.upsertEncounter(
                event.patientId(),
                event.appointmentId(),
                event.doctorId(),
                event.doctorName(),
                event.hospitalId(),
                event.hospitalName(),
                event.status(),
                event.startTime(),
                event.endTime()
        );
    }

    @KafkaListener(topics = "prescription-pdf-events",
            containerFactory = "prescriptionKafkaListenerContainerFactory",
            groupId = "timeline-service")
    public void consumePrescriptionEvent(PrescriptionPdfGeneratedEvent event) {
        ingestionService.addPrescriptionToEncounter(
                event.patientId(),
                event.appointmentId(),
                event.prescriptionId(),
                event.status()
        );
    }

    @KafkaListener(topics = "consent-events",
            containerFactory = "consentKafkaListenerContainerFactory",
            groupId = "timeline-service")
    public void consumeConsentGrantedEvent(ConsentGrantedEvent event) {
        String key = "consent:" + event.patientId() + ":" + event.hospitalId();
        redis.opsForValue().set(key, "granted", Duration.ofSeconds(event.ttlSeconds()));
    }

    @DltHandler
    public void handleDlt(Object event) {
        log.error("Timeline event moved to DLT after retries exhausted: type={}", event != null ? event.getClass().getSimpleName() : "null");
    }
}
