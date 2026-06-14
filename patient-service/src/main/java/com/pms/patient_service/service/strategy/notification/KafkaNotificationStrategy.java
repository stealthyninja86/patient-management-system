package com.pms.patient_service.service.strategy.notification;

import com.pms.patient_service.kafka.KafkaProducer;
import com.pms.patient_service.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationStrategy implements NotificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(KafkaNotificationStrategy.class);

    private final KafkaProducer kafkaProducer;

    public KafkaNotificationStrategy(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void notify(Patient patient) {
        log.info("Publishing patient event to Kafka: {}", patient.getPatientId());
        try {
            kafkaProducer.sendEvent(patient);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event: {}", e.getMessage());
        }
    }
}
