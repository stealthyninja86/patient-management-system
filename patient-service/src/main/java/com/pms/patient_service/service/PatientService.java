package com.pms.patient_service.service;

import com.pms.patient_service.dto.PatientRequestDTO;
import com.pms.patient_service.dto.PatientResponseDTO;
import com.pms.patient_service.exception.PatientNotFoundException;
import com.pms.patient_service.factory.PatientFactory;
import com.pms.patient_service.model.Patient;
import com.pms.patient_service.repository.PatientRepository;
import com.pms.patient_service.util.IdGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final IdGenerator idGenerator;

    public PatientService(PatientRepository patientRepository, IdGenerator idGenerator) {
        this.patientRepository = patientRepository;
        this.idGenerator = idGenerator;
    }

    public List<PatientResponseDTO> getAllPatients() {
        log.debug("Fetching all patients");
        return patientRepository.findAll().stream()
                .map(PatientFactory::toPatientResponseDTO)
                .toList();
    }

    public Patient createPatient(PatientRequestDTO dto) {
        log.debug("Creating new patient with email: {}", dto.getEmail());
        Patient patient = PatientFactory.createPatientEntity(dto);
        patient.setPatientId(idGenerator.nextId("PMS-", "patient_id_seq"));
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Patient existing, PatientRequestDTO dto) {
        log.debug("Updating patient with patientId: {}", existing.getPatientId());
        PatientFactory.updatePatientEntity(existing, dto);
        return patientRepository.save(existing);
    }

    public void deletePatient(Patient patient) {
        log.debug("Deleting patient with patientId: {}", patient.getPatientId());
        patientRepository.delete(patient);
    }

    public PatientResponseDTO getPatientByPatientId(String patientId) {
        log.debug("Fetching patient by patientId: {}", patientId);
        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found: " + patientId));
        return PatientFactory.toPatientResponseDTO(patient);
    }

    public PatientResponseDTO toResponseDTO(Patient patient) {
        log.debug("Converting Patient entity to response DTO for patientId: {}", patient.getPatientId());
        return PatientFactory.toPatientResponseDTO(patient);
    }
}
