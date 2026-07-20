package com.pms.patientservice.repository;

import com.pms.patientservice.model.Consent;
import com.pms.patientservice.model.ConsentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {
    Optional<Consent> findByConsentRequestId(String consentRequestId);
    boolean existsByPatientIdAndHospitalIdAndStatus(String patientId, String hospitalId, ConsentStatus status);
}
