package com.pms.patientservice.service.strategy;

import com.pms.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientEventOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PatientEventOrchestrator.class);

    private final List<PatientEventPublisher> publishers;

    public PatientEventOrchestrator(List<PatientEventPublisher> publishers) {
        this.publishers = publishers;
    }

    public void publishAll(Patient patient) {
        log.info("Notifying all publishers for patient: {}", patient.getPatientId());
        for (PatientEventPublisher publisher : publishers) {
            try {
                publisher.publish(patient);
            } catch (Exception e) {
                log.error("Patient event publisher failed: {}", e.getMessage());
            }
        }
    }
}
