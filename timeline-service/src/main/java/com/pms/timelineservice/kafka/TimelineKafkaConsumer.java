package com.pms.timelineservice.kafka;

import com.pms.timelineservice.dto.event.AppointmentEvent;
import com.pms.timelineservice.dto.event.ConsentGrantedEvent;
import com.pms.timelineservice.dto.event.PrescriptionPdfGeneratedEvent;
import com.pms.timelineservice.service.TimelineIngestionService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TimelineKafkaConsumer {

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
                event.hospitalName(),
                event.doctorName(),
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
}
