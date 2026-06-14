package com.pms.clinicalservice.repository;

import com.pms.clinicalservice.model.PrescriptionDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PrescriptionDocumentRepository extends JpaRepository<PrescriptionDocument, UUID> {
    Optional<PrescriptionDocument> findByPrescriptionId(String prescriptionId);
}
