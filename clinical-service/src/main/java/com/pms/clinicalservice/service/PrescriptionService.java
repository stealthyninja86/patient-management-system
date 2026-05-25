package com.pms.clinicalservice.service;

import com.pms.clinicalservice.dto.PrescriptionRequestDTO;
import com.pms.clinicalservice.dto.PrescriptionResponseDTO;
import com.pms.clinicalservice.exception.DepartmentNotFoundException;
import com.pms.clinicalservice.exception.DoctorNotFoundException;
import com.pms.clinicalservice.exception.PatientNotFoundException;
import com.pms.clinicalservice.exception.PrescriptionNotFoundException;
import com.pms.clinicalservice.factory.PrescriptionFactory;
import com.pms.clinicalservice.grpc.HospitalGrpcClient;
import com.pms.clinicalservice.grpc.PatientGrpcClient;
import com.pms.clinicalservice.model.Drug;
import com.pms.clinicalservice.model.Prescription;
import com.pms.clinicalservice.repository.DrugRepository;
import com.pms.clinicalservice.repository.PrescriptionRepository;
import com.pms.clinicalservice.util.IdGenerator;
import hospital.DepartmentResponse;
import hospital.DoctorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import patient.PatientResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final DrugRepository drugRepository;
    private final PatientGrpcClient patientGrpcClient;
    private final HospitalGrpcClient hospitalGrpcClient;
    private final IdGenerator idGenerator;
    private final PrescriptionFactory prescriptionFactory;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               DrugRepository drugRepository,
                               PatientGrpcClient patientGrpcClient,
                               HospitalGrpcClient hospitalGrpcClient,
                               IdGenerator idGenerator,
                               PrescriptionFactory prescriptionFactory) {
        this.prescriptionRepository = prescriptionRepository;
        this.drugRepository = drugRepository;
        this.patientGrpcClient = patientGrpcClient;
        this.hospitalGrpcClient = hospitalGrpcClient;
        this.idGenerator = idGenerator;
        this.prescriptionFactory = prescriptionFactory;
    }

    public List<PrescriptionResponseDTO> getAllPrescriptions() {
        log.debug("Fetching all prescriptions");
        return prescriptionRepository.findAll().stream()
                .map(prescriptionFactory::toPrescriptionResponseDTO)
                .collect(Collectors.toList());
    }

    public PrescriptionResponseDTO getPrescriptionById(String prescriptionId) {
        log.debug("Fetching prescription by id: {}", prescriptionId);
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + prescriptionId));
        return prescriptionFactory.toPrescriptionResponseDTO(prescription);
    }

    public List<PrescriptionResponseDTO> getPrescriptionsByPatientId(String patientId) {
        log.debug("Fetching prescriptions by patient id: {}", patientId);
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(prescriptionFactory::toPrescriptionResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponseDTO> getPrescriptionsByDoctorId(String doctorId) {
        log.debug("Fetching prescriptions by doctor id: {}", doctorId);
        return prescriptionRepository.findByDoctorId(doctorId).stream()
                .map(prescriptionFactory::toPrescriptionResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO request, String doctorId) {
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

        String prescriptionId = idGenerator.nextId("RX-", "prescription_id_seq");
        List<Drug> drugs = request.drugs().stream()
                .map(drugInput -> {
                    String drugId = idGenerator.nextId("DRG-", "drug_id_seq");
                    return prescriptionFactory.toDrugEntity(drugInput, drugId);
                })
                .collect(Collectors.toList());

        Prescription prescription = prescriptionFactory.toPrescriptionEntity(
                request, doctorId, doctorResponse, deptResponse, patientResponse, drugs);
        prescription.setPrescriptionId(prescriptionId);

        prescription = prescriptionRepository.save(prescription);
        return prescriptionFactory.toPrescriptionResponseDTO(prescription);
    }

    @Transactional
    public void deletePrescription(String prescriptionId) {
        log.debug("Deleting prescription: {}", prescriptionId);
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + prescriptionId));
        prescriptionRepository.delete(prescription);
    }
}
