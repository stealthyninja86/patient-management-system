package com.pms.patient_service.service;

import com.pms.patient_service.dto.request.PatientRequestDTO;
import com.pms.patient_service.exception.PatientNotFoundException;
import com.pms.patient_service.service.factory.PatientFactory;
import com.pms.patient_service.model.Patient;
import com.pms.patient_service.repository.PatientRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final PatientFactory patientFactory;

    public PatientService(PatientRepository patientRepository, PatientFactory patientFactory) {
        this.patientRepository = patientRepository;
        this.patientFactory = patientFactory;
    }

    public List<Patient> getAllPatients() {
        log.debug("Fetching all patients");
        return patientRepository.findAll();
    }

    public Patient createPatient(PatientRequestDTO dto) {
        log.debug("Creating new patient with email: {}", dto.getEmail());
        Patient patient = patientFactory.createPatient(dto);
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Patient existing, PatientRequestDTO dto) {
        log.debug("Updating patient with patientId: {}", existing.getPatientId());
        patientFactory.updatePatient(existing, dto);
        return patientRepository.save(existing);
    }

    public void deletePatient(Patient patient) {
        log.debug("Deleting patient with patientId: {}", patient.getPatientId());
        patientRepository.delete(patient);
    }

    public Patient getPatientByPatientId(String patientId) {
        log.debug("Fetching patient by patientId: {}", patientId);
        return patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + patientId));
    }
}
