package com.pms.patient_service.service.facade;

import com.pms.patient_service.dto.request.PatientGrpcRequestDTO;
import com.pms.patient_service.dto.request.PatientRequestDTO;
import com.pms.patient_service.dto.response.PatientResponseDTO;
import com.pms.patient_service.exception.EmailAlreadyExistsException;
import com.pms.patient_service.exception.PatientNotFoundException;
import com.pms.patient_service.service.mapper.PatientMapper;
import com.pms.patient_service.service.factory.PatientFactory;
import com.pms.patient_service.grpc.BillingServiceGrpcClient;
import com.pms.patient_service.model.Patient;
import com.pms.patient_service.repository.PatientRepository;
import com.pms.patient_service.service.PatientService;
import com.pms.patient_service.service.strategy.notification.NotificationOrchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class PatientFacade {

    private static final Logger log = LoggerFactory.getLogger(PatientFacade.class);

    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final NotificationOrchestrator notificationOrchestrator;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final PatientMapper patientMapper;
    private final PatientFactory patientFactory;

    public PatientFacade(PatientService patientService,
                         PatientRepository patientRepository,
                         NotificationOrchestrator notificationOrchestrator,
                         BillingServiceGrpcClient billingServiceGrpcClient,
                         PatientMapper patientMapper,
                         PatientFactory patientFactory) {
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.notificationOrchestrator = notificationOrchestrator;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.patientMapper = patientMapper;
        this.patientFactory = patientFactory;
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
        billingServiceGrpcClient.createBillingAccount(created.getPatientId(), created.getName(), created.getEmail());
        notificationOrchestrator.notifyAll(created);
        return patientMapper.toResponseDTO(created);
    }

    @Transactional
    public PatientResponseDTO createPatient(PatientGrpcRequestDTO dto) {
        log.debug("Facade: creating patient via gRPC with email: {}", dto.email());
        if (patientRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("A patient with this email " + dto.email() + " already exists");
        }
        Patient patient = patientFactory.createPatient(dto);
        Patient saved = patientRepository.save(patient);
        billingServiceGrpcClient.createBillingAccount(saved.getPatientId(), saved.getName(), saved.getEmail());
        notificationOrchestrator.notifyAll(saved);
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
}
