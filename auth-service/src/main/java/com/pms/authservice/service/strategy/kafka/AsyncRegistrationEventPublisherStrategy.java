package com.pms.authservice.service.strategy.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.authservice.dto.event.AdminRegisteredEvent;
import com.pms.authservice.dto.event.DoctorRegisteredEvent;
import com.pms.authservice.dto.event.PatientRegisteredEvent;
import com.pms.authservice.model.OutboxEvent;
import com.pms.authservice.repository.OutboxRepository;
import com.pms.authservice.service.strategy.RegistrationEventPublisherStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AsyncRegistrationEventPublisherStrategy implements RegistrationEventPublisherStrategy {

    private static final Logger log = LoggerFactory.getLogger(AsyncRegistrationEventPublisherStrategy.class);
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public AsyncRegistrationEventPublisherStrategy(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(Object event) {
        try {
            String topic = topicFor(event);
            String aggregateId = aggregateIdFrom(event);
            String eventType = eventTypeFor(event);
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                UUID.randomUUID(),
                "USER_REGISTRATION",
                aggregateId,
                eventType,
                topic,
                payload,
                emailFrom(event),
                false,
                LocalDateTime.now(),
                null
            );
            outboxRepository.save(outboxEvent);
            log.debug("Outbox event saved: {} for {}", eventType, aggregateId);
        } catch (Exception e) {
            log.error("Failed to write outbox event for registration", e);
            throw new RuntimeException(e);
        }
    }

    private String topicFor(Object event) {
        if (event instanceof AdminRegisteredEvent) return "user-registrations.admin";
        if (event instanceof DoctorRegisteredEvent) return "user-registrations.doctor";
        if (event instanceof PatientRegisteredEvent) return "user-registrations.patient";
        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }

    private String eventTypeFor(Object event) {
        if (event instanceof AdminRegisteredEvent) return "ADMIN_REGISTERED";
        if (event instanceof DoctorRegisteredEvent) return "DOCTOR_REGISTERED";
        if (event instanceof PatientRegisteredEvent) return "PATIENT_REGISTERED";
        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }

    private String aggregateIdFrom(Object event) {
        if (event instanceof AdminRegisteredEvent e) return e.email();
        if (event instanceof DoctorRegisteredEvent e) return e.doctorId();
        if (event instanceof PatientRegisteredEvent e) return e.patientId();
        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }

    private String emailFrom(Object event) {
        if (event instanceof AdminRegisteredEvent e) return e.email();
        if (event instanceof DoctorRegisteredEvent e) return e.email();
        if (event instanceof PatientRegisteredEvent e) return e.email();
        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }
}
