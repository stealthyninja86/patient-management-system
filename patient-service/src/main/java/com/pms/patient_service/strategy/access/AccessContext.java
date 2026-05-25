package com.pms.patient_service.strategy.access;

import com.pms.patient_service.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccessContext {

    private static final Logger log = LoggerFactory.getLogger(AccessContext.class);

    private final AccessStrategyProvider strategyProvider;

    public AccessContext(AccessStrategyProvider strategyProvider) {
        this.strategyProvider = strategyProvider;
    }

    public boolean canAccessPatient(Patient patient, String userId, String role) {
        log.debug("Access context: checking access for patientId: {}, userId: {}, role: {}", patient.getPatientId(), userId, role);
        AccessStrategy strategy = strategyProvider.getStrategy(role);
        return strategy.canAccess(patient, userId);
    }

    public List<Patient> filterPatients(List<Patient> patients, String userId, String role) {
        log.debug("Access context: filtering {} patients for userId: {}, role: {}", patients.size(), userId, role);
        AccessStrategy strategy = strategyProvider.getStrategy(role);
        return strategy.filter(patients, userId);
    }
}
