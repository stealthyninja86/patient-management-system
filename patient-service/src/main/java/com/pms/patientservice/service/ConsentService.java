package com.pms.patientservice.service;

import com.pms.patientservice.exception.ConsentNotFoundException;
import com.pms.patientservice.model.Consent;
import com.pms.patientservice.model.ConsentStatus;
import com.pms.patientservice.repository.ConsentRepository;
import com.pms.patientservice.service.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentService {

    private static final Logger log = LoggerFactory.getLogger(ConsentService.class);

    private final ConsentRepository consentRepository;
    private final IdGenerator idGenerator;

    public ConsentService(ConsentRepository consentRepository,
                          IdGenerator idGenerator) {
        this.consentRepository = consentRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public Consent createConsent(String patientId, String doctorId, String hospitalId) {
        log.info("Creating consent for patient: {}, doctor: {}, hospital: {}",
                patientId, doctorId, hospitalId);

        String consentRequestId = idGenerator.nextId("CSNT", "consent_seq");
        Consent consent = new Consent();
        consent.setConsentRequestId(consentRequestId);
        consent.setPatientId(patientId);
        consent.setDoctorId(doctorId);
        consent.setHospitalId(hospitalId);
        consent.setStatus(ConsentStatus.PENDING_OTP);
        return consentRepository.save(consent);
    }

    @Transactional
    public Consent activateConsent(String consentRequestId) {
        log.info("Activating consent: {}", consentRequestId);
        Consent consent = consentRepository.findByConsentRequestId(consentRequestId)
                .orElseThrow(() -> new ConsentNotFoundException(
                        "Consent not found: " + consentRequestId));
        consent.setStatus(ConsentStatus.ACTIVE);
        return consentRepository.save(consent);
    }

    public Consent getByConsentRequestId(String consentRequestId) {
        return consentRepository.findByConsentRequestId(consentRequestId)
                .orElseThrow(() -> new ConsentNotFoundException(
                        "Consent not found: " + consentRequestId));
    }
}
