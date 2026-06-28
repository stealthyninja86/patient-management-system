package com.pms.clinicalservice.service;

import com.pms.clinicalservice.dto.request.DoctorContactUpdateDTO;
import com.pms.clinicalservice.dto.request.HospitalContactUpdateDTO;
import com.pms.clinicalservice.dto.request.PatientContactUpdateDTO;
import com.pms.clinicalservice.dto.response.PrescriptionResponseDTO;
import com.pms.clinicalservice.exception.PrescriptionNotFoundException;
import com.pms.clinicalservice.service.mapper.PrescriptionMapper;
import com.pms.clinicalservice.model.Prescription;
import com.pms.clinicalservice.repository.PrescriptionRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMapper prescriptionMapper;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PrescriptionMapper prescriptionMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionMapper = prescriptionMapper;
    }

    public List<PrescriptionResponseDTO> getAllPrescriptions() {
        log.debug("Fetching all prescriptions");
        return prescriptionRepository.findAll().stream()
                .map(prescriptionMapper::toPrescriptionResponseDTO)
                .collect(Collectors.toList());
    }

    public PrescriptionResponseDTO getPrescriptionById(String prescriptionId) {
        log.debug("Fetching prescription by id: {}", prescriptionId);
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + prescriptionId));
        return prescriptionMapper.toPrescriptionResponseDTO(prescription);
    }

    public List<PrescriptionResponseDTO> getPrescriptionsByPatientId(String patientId) {
        log.debug("Fetching prescriptions by patient id: {}", patientId);
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(prescriptionMapper::toPrescriptionResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponseDTO> getPrescriptionsByDoctorId(String doctorId) {
        log.debug("Fetching prescriptions by doctor id: {}", doctorId);
        return prescriptionRepository.findByDoctorId(doctorId).stream()
                .map(prescriptionMapper::toPrescriptionResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponseDTO> searchPrescriptions(
            String hospitalId, String doctorId, LocalDate startDate, LocalDate endDate) {
        log.debug("Searching prescriptions: hospitalId={}, doctorId={}, startDate={}, endDate={}",
                hospitalId, doctorId, startDate, endDate);
        Specification<Prescription> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hospitalId != null && !hospitalId.isBlank()) {
                predicates.add(cb.equal(root.get("hospitalId"), hospitalId));
            }
            if (doctorId != null && !doctorId.isBlank()) {
                predicates.add(cb.equal(root.get("doctorId"), doctorId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("consultationDate"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("consultationDate"), endDate.atTime(LocalTime.MAX)));
            }
            query.orderBy(cb.desc(root.get("consultationDate")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return prescriptionRepository.findAll(spec).stream()
                .map(prescriptionMapper::toPrescriptionResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionResponseDTO updatePatientContact(String prescriptionId, PatientContactUpdateDTO update) {
        log.debug("Updating patient contact info for prescription: {}", prescriptionId);
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + prescriptionId));
        if (update.patientPhone() != null) {
            prescription.setPatientPhone(update.patientPhone());
        }
        if (update.patientEmail() != null) {
            prescription.setPatientEmail(update.patientEmail());
        }
        prescription = prescriptionRepository.save(prescription);
        return prescriptionMapper.toPrescriptionResponseDTO(prescription);
    }

    @Transactional
    public PrescriptionResponseDTO updateHospitalContact(String prescriptionId, HospitalContactUpdateDTO update) {
        log.debug("Updating hospital contact info for prescription: {}", prescriptionId);
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + prescriptionId));
        if (update.hospitalPhone() != null) {
            prescription.setHospitalPhone(update.hospitalPhone());
        }
        if (update.hospitalEmail() != null) {
            prescription.setHospitalEmail(update.hospitalEmail());
        }
        if (update.hospitalWebsite() != null) {
            prescription.setHospitalWebsite(update.hospitalWebsite());
        }
        prescription = prescriptionRepository.save(prescription);
        return prescriptionMapper.toPrescriptionResponseDTO(prescription);
    }

    @Transactional
    public PrescriptionResponseDTO updateDoctorContact(String prescriptionId, DoctorContactUpdateDTO update) {
        log.debug("Updating doctor contact info for prescription: {}", prescriptionId);
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + prescriptionId));
        if (update.doctorPhone() != null) {
            prescription.setDoctorPhone(update.doctorPhone());
        }
        if (update.doctorEmail() != null) {
            prescription.setDoctorEmail(update.doctorEmail());
        }
        prescription = prescriptionRepository.save(prescription);
        return prescriptionMapper.toPrescriptionResponseDTO(prescription);
    }

    @Transactional
    public void deletePrescription(String prescriptionId) {
        log.debug("Deleting prescription: {}", prescriptionId);
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found: " + prescriptionId));
        prescriptionRepository.delete(prescription);
    }
}
