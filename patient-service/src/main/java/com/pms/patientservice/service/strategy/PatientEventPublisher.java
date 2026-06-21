package com.pms.patientservice.service.strategy;

import com.pms.patientservice.model.Patient;

public interface PatientEventPublisher {
    void publish(Patient patient);
}
