package com.pms.patient_service.service.strategy.access;

import com.pms.patient_service.model.Patient;

import java.util.List;

public interface AccessStrategy {
    boolean canAccess(Patient patient, String userId);
    List<Patient> filter(List<Patient> patients, String userId);
}
