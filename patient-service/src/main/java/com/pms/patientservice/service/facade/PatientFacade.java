package com.pms.patientservice.service.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.patientservice.dto.request.PatientGrpcRequestDTO;
import com.pms.patientservice.dto.request.PatientRequestDTO;
import com.pms.patientservice.dto.response.PatientResponseDTO;
import com.pms.patientservice.exception.EmailAlreadyExistsException;
import com.pms.patientservice.exception.PatientNotFoundException;
import com.pms.patientservice.service.mapper.PatientMapper;
import com.pms.patientservice.grpc.BillingGrpcClient;
import com.pms.patientservice.model.Patient;
import com.pms.patientservice.model.OutboxEvent;
import com.pms.patientservice.repository.OutboxRepository;
import com.pms.patientservice.repository.PatientRepository;
import com.pms.patientservice.service.PatientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PatientFacade {

    private static final Logger log = LoggerFactory.getLogger(PatientFacade.class);

    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final BillingGrpcClient billingGrpcClient;
    private final PatientMapper patientMapper;

    public PatientFacade(PatientService patientService,
                         PatientRepository patientRepository,
                         OutboxRepository outboxRepository,
                         ObjectMapper objectMapper,
                         BillingGrpcClient billingGrpcClient,
                         PatientMapper patientMapper) {
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.billingGrpcClient = billingGrpcClient;
        this.patientMapper = patientMapper;
    }

    public List<PatientResponseDTO> getAllPatients() {
        log.debug("Facade: fetching all patients");
        return patientService.getAllPatients().stream()
                .map(patientMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO dto) {
        log.debug("Facade: creating patient with email: {}", dto.getEmail());
        if (patientRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email " + dto.getEmail() + " already exists");
        }
        Patient created = patientService.createPatient(dto);
        billingGrpcClient.createBillingAccount(created.getPatientId(), created.getName(), created.getEmail());
        writePatientCreatedEvent(created);
        return patientMapper.toResponseDTO(created);
    }

    @Transactional
    public PatientResponseDTO createPatient(PatientGrpcRequestDTO dto) {
        log.debug("Facade: creating patient via gRPC with email: {}", dto.email());
        if (patientRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("A patient with this email " + dto.email() + " already exists");
        }
        Patient patient = patientMapper.createPatient(dto);
        Patient saved = patientRepository.save(patient);
        billingGrpcClient.createBillingAccount(saved.getPatientId(), saved.getName(), saved.getEmail());
        writePatientCreatedEvent(saved);
        return patientMapper.toResponseDTO(saved);
    }

    @Transactional
    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO dto) {
        log.debug("Facade: updating patient with id: {}", id);
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));
        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            if (patientRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
                throw new EmailAlreadyExistsException("A patient with this email " + dto.getEmail() + " already exists");
            }
        }
        Patient updated = patientService.updatePatient(existing, dto);
        return patientMapper.toResponseDTO(updated);
    }

    public void deletePatient(UUID id) {
        log.debug("Facade: deleting patient with id: {}", id);
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));
        patientService.deletePatient(existing);
    }

    public PatientResponseDTO getPatientByPatientId(String patientId) {
        log.debug("Facade: fetching patient by patientId: {}", patientId);
        if (patientId == null || patientId.isBlank()) {
            throw new PatientNotFoundException("Patient ID is required");
        }
        return patientMapper.toResponseDTO(patientService.getPatientByPatientId(patientId));
    }

    private void writePatientCreatedEvent(Patient patient) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                "patientId", patient.getPatientId(),
                "name", patient.getName(),
                "email", patient.getEmail(),
                "eventType", "PATIENT_CREATED"
            ));
            OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "PATIENT",
                patient.getPatientId(),
                "PATIENT_CREATED",
                "patient",
                payload,
                patient.getPatientId(),
                false,
                LocalDateTime.now(),
                null
            );
            outboxRepository.save(event);
            log.debug("Outbox event saved: PATIENT_CREATED for patient: {}", patient.getPatientId());
        } catch (Exception e) {
            log.error("Failed to write outbox event for patient: {}", patient.getPatientId(), e);
        }
    }
}
