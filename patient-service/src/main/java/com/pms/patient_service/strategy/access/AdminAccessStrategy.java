package com.pms.patient_service.strategy.access;

import com.pms.patient_service.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminAccessStrategy implements AccessStrategy {

    private static final Logger log = LoggerFactory.getLogger(AdminAccessStrategy.class);

    @Override
    public boolean canAccess(Patient patient, String userId) {
        log.debug("Admin access check for patientId: {}, userId: {} - granted", patient.getPatientId(), userId);
        return true;
    }

    @Override
    public List<Patient> filter(List<Patient> patients, String userId) {
        log.debug("Admin filter for userId: {} - returning all {} patients", userId, patients.size());
        return patients;
    }
}
