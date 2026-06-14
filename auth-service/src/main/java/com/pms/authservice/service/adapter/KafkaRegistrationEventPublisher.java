package com.pms.authservice.service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.authservice.dto.event.AdminRegisteredEvent;
import com.pms.authservice.dto.event.DoctorRegisteredEvent;
import com.pms.authservice.dto.event.PatientRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "registration.event.publisher", havingValue = "kafka")
public class KafkaRegistrationEventPublisher implements RegistrationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaRegistrationEventPublisher.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaRegistrationEventPublisher(KafkaTemplate<String, byte[]> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(Object event) {
        String topic = topicFor(event);
        String email = emailFrom(event);
        try {
            byte[] data = objectMapper.writeValueAsBytes(event);
            kafkaTemplate.send(topic, email, data)
                    .whenComplete((res, e) -> {
                        if (e != null) {
                            log.error("Error sending registration: {} error: {}", email, e.getMessage());
                        } else {
                            log.debug("Registration sent: {} at offset {} to {} successfully",
                                    email, res.getRecordMetadata().offset(), topic);
                        }
                    });
        } catch (Exception e) {
            log.error("failed to serialize registration event for {}", email, e);
            throw new RuntimeException(e);
        }
    }

    private String topicFor(Object event) {
        if (event instanceof AdminRegisteredEvent) return "user-registrations.admin";
        if (event instanceof DoctorRegisteredEvent) return "user-registrations.doctor";
        if (event instanceof PatientRegisteredEvent) return "user-registrations.patient";
        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }

    private String emailFrom(Object event) {
        if (event instanceof AdminRegisteredEvent e) return e.email();
        if (event instanceof DoctorRegisteredEvent e) return e.email();
        if (event instanceof PatientRegisteredEvent e) return e.email();
        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }
}
