package com.pms.patient_service.strategy.access;

import com.pms.patient_service.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DoctorAccessStrategy implements AccessStrategy {

    private static final Logger log = LoggerFactory.getLogger(DoctorAccessStrategy.class);

    @Override
    public boolean canAccess(Patient patient, String userId) {
        log.debug("Doctor access check for patientId: {}, userId: {}", patient.getPatientId(), userId);
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return true;
    }

    @Override
    public List<Patient> filter(List<Patient> patients, String userId) {
        log.debug("Doctor filter for userId: {} - returning all {} patients", userId, patients.size());
        return patients;
    }
}
