package com.pms.patient_service.service.strategy.access;

import com.pms.patient_service.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientAccessStrategy implements AccessStrategy {

    private static final Logger log = LoggerFactory.getLogger(PatientAccessStrategy.class);

    @Override
    public boolean canAccess(Patient patient, String userId) {
        log.debug("Patient access check for patientId: {}, userId: {}", patient.getPatientId(), userId);
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return userId.equals(patient.getPatientId());
    }

    @Override
    public List<Patient> filter(List<Patient> patients, String userId) {
        log.debug("Patient filter for userId: {} - filtering {} patients", userId, patients.size());
        return patients.stream()
                .filter(p -> userId.equals(p.getPatientId()))
                .toList();
    }
}
