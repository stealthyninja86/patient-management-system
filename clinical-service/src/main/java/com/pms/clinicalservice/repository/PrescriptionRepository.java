package com.pms.clinicalservice.repository;

import com.pms.clinicalservice.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    List<Prescription> findByDoctorId(String doctorId);
    List<Prescription> findByPatientId(String patientId);
    Optional<Prescription> findByPrescriptionId(String prescriptionId);
}
