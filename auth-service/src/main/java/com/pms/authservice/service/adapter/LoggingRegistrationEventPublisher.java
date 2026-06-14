package com.pms.authservice.service.adapter;

import com.pms.authservice.dto.event.AdminRegisteredEvent;
import com.pms.authservice.dto.event.DoctorRegisteredEvent;
import com.pms.authservice.dto.event.PatientRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "registration.event.publisher", havingValue = "logging", matchIfMissing = true)
public class LoggingRegistrationEventPublisher implements RegistrationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingRegistrationEventPublisher.class);

    @Override
    public void publish(Object event) {
        switch (event) {
            case AdminRegisteredEvent e ->
                log.info("Admin registered: email={}", e.email());
            case DoctorRegisteredEvent e ->
                log.info("Doctor registered: email={}, doctorId={}, hospitalId={}",
                        e.email(), e.doctorId(), e.hospitalId());
            case PatientRegisteredEvent e ->
                log.info("Patient registered: email={}, patientId={}",
                        e.email(), e.patientId());
            default ->
                log.warn("Unknown registration event: {}", event.getClass());
        }
    }
}
