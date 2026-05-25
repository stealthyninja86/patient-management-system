package com.pms.patient_service.strategy.notification;

import com.pms.patient_service.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(NotificationOrchestrator.class);

    private final List<NotificationStrategy> strategies;

    public NotificationOrchestrator(List<NotificationStrategy> strategies) {
        this.strategies = strategies;
    }

    public void notifyAll(Patient patient) {
        log.info("Notifying all strategies for patient: {}", patient.getPatientId());
        for (NotificationStrategy strategy : strategies) {
            try {
                strategy.notify(patient);
            } catch (Exception e) {
                log.error("Notification strategy failed: {}", e.getMessage());
            }
        }
    }
}
