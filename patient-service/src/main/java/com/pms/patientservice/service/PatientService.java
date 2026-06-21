package com.pms.patientservice.service;

import com.pms.patientservice.dto.request.PatientRequestDTO;
import com.pms.patientservice.exception.PatientNotFoundException;
import com.pms.patientservice.service.mapper.PatientMapper;
import com.pms.patientservice.model.Patient;
import com.pms.patientservice.repository.PatientRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    public List<Patient> getAllPatients() {
        log.debug("Fetching all patients");
        return patientRepository.findAll();
    }

    public Patient createPatient(PatientRequestDTO dto) {
        log.debug("Creating new patient with email: {}", dto.getEmail());
        Patient patient = patientMapper.createPatient(dto);
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Patient existing, PatientRequestDTO dto) {
        log.debug("Updating patient with patientId: {}", existing.getPatientId());
        patientMapper.updatePatient(existing, dto);
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
