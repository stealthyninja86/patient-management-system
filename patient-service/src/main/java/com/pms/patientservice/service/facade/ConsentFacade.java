package com.pms.patientservice.service.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.patientservice.dto.event.ConsentGrantedEvent;
import com.pms.patientservice.dto.event.ConsentRevokedEvent;
import com.pms.patientservice.dto.request.ConsentRequestDTO;
import com.pms.patientservice.grpc.OtpGrpcClient;
import com.pms.patientservice.model.Consent;
import com.pms.patientservice.model.OutboxEvent;
import com.pms.patientservice.repository.OutboxRepository;
import com.pms.patientservice.service.ConsentService;
import notification.OptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class ConsentFacade {

    private static final Logger log = LoggerFactory.getLogger(ConsentFacade.class);

    private static final int CONSENT_TTL_SECONDS = 604800;

    private final ConsentService consentService;
    private final OtpGrpcClient otpGrpcClient;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ConsentFacade(ConsentService consentService,
                         OtpGrpcClient otpGrpcClient,
                         OutboxRepository outboxRepository,
                         ObjectMapper objectMapper) {
        this.consentService = consentService;
        this.otpGrpcClient = otpGrpcClient;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, Object> requestConsent(ConsentRequestDTO request) {
        log.debug("Requesting consent for patient: {}, hospital: {}",
                request.patientId(), request.hospitalId());

        Consent consent = consentService.createConsent(
                request.patientId(), request.hospitalId());

        OptService.GenerateOtpRequest otpRequest = OptService.GenerateOtpRequest.newBuilder()
                .setDomainKey("consent:" + consent.getConsentRequestId())
                .setPhoneNumber(request.phoneNumber() != null ? request.phoneNumber() : "")
                .setEmail(request.email() != null ? request.email() : "")
                .setNotificationType("CONSENT_OTP")
                .build();

        otpGrpcClient.generateOtp(otpRequest);

        return Map.of(
                "consentRequestId", consent.getConsentRequestId(),
                "message", "OTP sent to registered phone/email"
        );
    }

    public boolean isOwnConsent(String consentRequestId, String patientId) {
        if (patientId == null) return false;
        try {
            Consent consent = consentService.getByConsentRequestId(consentRequestId);
            return patientId.equals(consent.getPatientId());
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public Map<String, Object> revokeConsent(String consentRequestId) {
        log.debug("Revoking consent: {}", consentRequestId);
        Consent consent = consentService.revokeConsent(consentRequestId);
        writeConsentRevokedEvent(consent);
        return Map.of(
                "status", "REVOKED",
                "message", "Consent revoked"
        );
    }

    private void writeConsentGrantedEvent(Consent consent) {
        try {
            ConsentGrantedEvent event = new ConsentGrantedEvent(
                    consent.getPatientId(), consent.getHospitalId(), CONSENT_TTL_SECONDS);
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.randomUUID(),
                    "CONSENT",
                    consent.getConsentRequestId(),
                    "CONSENT_GRANTED",
                    "consent-events",
                    payload,
                    consent.getPatientId(),
                    false,
                    LocalDateTime.now(),
                    null
            );
            outboxRepository.save(outboxEvent);
            log.debug("Outbox event saved: CONSENT_GRANTED for consent: {}", consent.getConsentRequestId());
        } catch (Exception e) {
            log.error("Failed to write outbox event for consent: {}", consent.getConsentRequestId(), e);
        }
    }

    private void writeConsentRevokedEvent(Consent consent) {
        try {
            ConsentRevokedEvent event = new ConsentRevokedEvent(
                    consent.getPatientId(), consent.getHospitalId());
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.randomUUID(),
                    "CONSENT",
                    consent.getConsentRequestId(),
                    "CONSENT_REVOKED",
                    "consent-revoked-events",
                    payload,
                    consent.getPatientId(),
                    false,
                    LocalDateTime.now(),
                    null
            );
            outboxRepository.save(outboxEvent);
            log.debug("Outbox event saved: CONSENT_REVOKED for consent: {}", consent.getConsentRequestId());
        } catch (Exception e) {
            log.error("Failed to write outbox event for consent revoke: {}", consent.getConsentRequestId(), e);
        }
    }

    @Transactional
    public Map<String, Object> confirmConsent(String consentRequestId, String code) {
        log.debug("Confirming consent: {}", consentRequestId);

        OptService.VerifyOtpRequest verifyRequest = OptService.VerifyOtpRequest.newBuilder()
                .setDomainKey("consent:" + consentRequestId)
                .setCode(code)
                .build();

        OptService.VerifyOtpResponse response = otpGrpcClient.verifyOtp(verifyRequest);

        if (!response.getVerified()) {
            return Map.of(
                    "verified", false,
                    "status", response.getStatus(),
                    "message", "Verification failed"
            );
        }

        consentService.activateConsent(consentRequestId);
        Consent consent = consentService.getByConsentRequestId(consentRequestId);

        writeConsentGrantedEvent(consent);

        return Map.of(
                "verified", true,
                "status", "ACTIVE",
                "message", "Consent granted"
        );
    }
}
