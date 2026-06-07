package com.pms.clinicalservice.facade;

import com.pms.clinicalservice.dto.PrescriptionRequestDTO;
import com.pms.clinicalservice.dto.PrescriptionResponseDTO;
import com.pms.clinicalservice.exception.*;
import com.pms.clinicalservice.factory.PrescriptionFactory;
import com.pms.clinicalservice.grpc.HospitalGrpcClient;
import com.pms.clinicalservice.grpc.PatientGrpcClient;
import com.pms.clinicalservice.grpc.ScheduleGrpcClient;
import com.pms.clinicalservice.grpc.ScheduleGrpcClient.OngoingAppointmentResult;
import com.pms.clinicalservice.kafka.PrescriptionPdfTaskEvent;
import com.pms.clinicalservice.model.DocumentStatus;
import com.pms.clinicalservice.model.Drug;
import com.pms.clinicalservice.model.Prescription;
import com.pms.clinicalservice.model.PrescriptionDocument;
import com.pms.clinicalservice.repository.PrescriptionDocumentRepository;
import com.pms.clinicalservice.repository.PrescriptionRepository;
import com.pms.clinicalservice.service.PrescriptionService;
import com.pms.clinicalservice.service.storage.StorageService;
import com.pms.clinicalservice.util.IdGenerator;
import hospital.DepartmentResponse;
import hospital.DoctorResponse;
import hospital.HospitalResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import patient.PatientResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PrescriptionFacade {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionFacade.class);

    private final PrescriptionService prescriptionService;
    private final PrescriptionFactory prescriptionFactory;
    private final PrescriptionRepository prescriptionRepository;
    private final ScheduleGrpcClient scheduleGrpcClient;
    private final HospitalGrpcClient hospitalGrpcClient;
    private final PatientGrpcClient patientGrpcClient;
    private final IdGenerator idGenerator;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PrescriptionDocumentRepository prescriptionDocumentRepository;
    private final StorageService storageService;

    public PrescriptionFacade(PrescriptionService prescriptionService,
                              PrescriptionFactory prescriptionFactory,
                              PrescriptionRepository prescriptionRepository,
                              ScheduleGrpcClient scheduleGrpcClient,
                              HospitalGrpcClient hospitalGrpcClient,
                              PatientGrpcClient patientGrpcClient,
                              IdGenerator idGenerator,
                              KafkaTemplate<String, Object> kafkaTemplate,
                              PrescriptionDocumentRepository prescriptionDocumentRepository,
                              StorageService storageService) {
        this.prescriptionService = prescriptionService;
        this.prescriptionFactory = prescriptionFactory;
        this.prescriptionRepository = prescriptionRepository;
        this.scheduleGrpcClient = scheduleGrpcClient;
        this.hospitalGrpcClient = hospitalGrpcClient;
        this.patientGrpcClient = patientGrpcClient;
        this.idGenerator = idGenerator;
        this.kafkaTemplate = kafkaTemplate;
        this.prescriptionDocumentRepository = prescriptionDocumentRepository;
        this.storageService = storageService;
    }

    @Transactional
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO request, String doctorId) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            Optional<Prescription> existing = prescriptionRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) {
                log.info("Idempotency hit for key: {}, returning existing prescription: {}", request.idempotencyKey(), existing.get().getPrescriptionId());
                return prescriptionFactory.toPrescriptionResponseDTO(existing.get());
            }
        }

        OngoingAppointmentResult appointmentResult = checkOngoingAppointment(doctorId, request.patientId());
        if (!appointmentResult.hasOngoing()) {
            throw new InvalidPatientPrescriptionOperationException(
                    "Patient " + request.patientId() + " is not currently under your care."
            );
        }

        log.info("Creating prescription for patient {} by doctor {}", request.patientId(), doctorId);
        DoctorResponse doctorResponse;
        try {
            doctorResponse = hospitalGrpcClient.getDoctorById(doctorId);
        } catch (Exception e) {
            throw new DoctorNotFoundException("Doctor not found with id: " + doctorId);
        }

        DepartmentResponse deptResponse;
        try {
            deptResponse = hospitalGrpcClient.getDepartmentById(doctorResponse.getDepartmentId());
        } catch (Exception e) {
            throw new DepartmentNotFoundException("Department not found for doctor: " + doctorId);
        }

        PatientResponse patientResponse;
        try {
            patientResponse = patientGrpcClient.getPatientById(request.patientId());
        } catch (Exception e) {
            throw new PatientNotFoundException("Patient not found with id: " + request.patientId());
        }

        HospitalResponse hospitalResponse;
        try {
            hospitalResponse = hospitalGrpcClient.getHospitalById(deptResponse.getHospitalId());
        } catch (Exception e) {
            throw new RuntimeException("Hospital not found for department: " + deptResponse.getDepartmentId());
        }

        String prescriptionId = idGenerator.nextId("RX-", "prescription_id_seq");
        List<Drug> drugs = request.drugs() != null
                ? request.drugs().stream()
                    .map(drugInput -> {
                        String drugId = idGenerator.nextId("DRG-", "drug_id_seq");
                        return prescriptionFactory.toDrugEntity(drugInput, drugId);
                    })
                    .collect(Collectors.toList())
                : List.of();

        LocalDateTime consultationDate = appointmentResult.appointmentTime() != null
                ? appointmentResult.appointmentTime()
                : LocalDateTime.now();

        Prescription prescription = prescriptionFactory.toPrescriptionEntity(
                request, doctorId, doctorResponse, deptResponse, hospitalResponse, patientResponse,
                drugs, consultationDate);
        prescription.setPrescriptionId(prescriptionId);

        try {
            prescription = prescriptionRepository.save(prescription);
        } catch (DataIntegrityViolationException e) {
            if (request.idempotencyKey() != null) {
                Optional<Prescription> existing = prescriptionRepository.findByIdempotencyKey(request.idempotencyKey());
                if (existing.isPresent()) {
                    log.warn("Race condition: idempotency key {} already persisted, returning existing", request.idempotencyKey());
                    return prescriptionFactory.toPrescriptionResponseDTO(existing.get());
                }
            }
            throw e;
        }

        enqueuePdfGeneration(prescriptionId);

        return prescriptionFactory.toPrescriptionResponseDTO(prescription);
    }

    @CircuitBreaker(name = "scheduleService", fallbackMethod = "checkAppointmentFallback")
    public OngoingAppointmentResult checkOngoingAppointment(String doctorId, String patientId) {
        return scheduleGrpcClient.checkOngoingAppointment(doctorId, patientId);
    }

    private OngoingAppointmentResult checkAppointmentFallback(String doctorId, String patientId, Throwable t) {
        log.error("Schedule-service unavailable, blocking prescription for doctor: {}, patient: {}", doctorId, patientId, t);
        throw new ServiceUnavailableException("Cannot verify doctor-patient relationship. Please try again.");
    }

    public void enqueuePdfGeneration(String prescriptionId) {
        PrescriptionDocument doc = prescriptionDocumentRepository.findByPrescriptionId(prescriptionId)
                .orElse(PrescriptionDocument.builder()
                        .prescriptionId(prescriptionId)
                        .fileName(prescriptionId + ".pdf")
                        .status(DocumentStatus.PENDING)
                        .build());

        doc.setStatus(DocumentStatus.PENDING);
        doc.setGeneratedAt(null);
        prescriptionDocumentRepository.save(doc);

        kafkaTemplate.send("prescription-pdf-tasks", prescriptionId, new PrescriptionPdfTaskEvent(prescriptionId));
    }

    public ResponseEntity<?> getPrescriptionPdf(String prescriptionId) {
        log.info("Getting prescription pdf for id: {}", prescriptionId);

        PrescriptionDocument doc = prescriptionDocumentRepository.findByPrescriptionId(prescriptionId).orElse(null);

        if (doc == null || doc.getStatus() == DocumentStatus.PENDING) {
            return ResponseEntity.accepted()
                    .header(HttpHeaders.LOCATION, "/prescriptions/" + prescriptionId + "/pdf")
                    .header("Retry-After", "5")
                    .body("Pdf is being generated, Retry after 5 seconds");
        }

        if (doc.getStatus() == DocumentStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("pdf generation failed. Please retry via POST");
        }

        try {
            byte[] pdfBytes = storageService.retrieve(doc.getFileName());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            log.error("Failed to get pdf for id: {}", prescriptionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("pdf generation failed. Please retry via POST");
        }
    }
}
