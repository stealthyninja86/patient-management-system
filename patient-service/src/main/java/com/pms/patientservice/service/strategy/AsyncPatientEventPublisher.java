package com.pms.patientservice.service.strategy;

import com.pms.patientservice.kafka.PatientProducer;
import com.pms.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AsyncPatientEventPublisher implements PatientEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AsyncPatientEventPublisher.class);

    private final PatientProducer patientProducer;

    public AsyncPatientEventPublisher(PatientProducer patientProducer) {
        this.patientProducer = patientProducer;
    }

    @Override
    public void publish(Patient patient) {
        log.info("Publishing patient event to Kafka: {}", patient.getPatientId());
        try {
            patientProducer.sendEvent(patient);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event: {}", e.getMessage());
        }
    }
}
