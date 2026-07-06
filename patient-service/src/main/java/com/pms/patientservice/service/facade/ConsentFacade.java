package com.pms.patientservice.service.facade;

import com.pms.patientservice.dto.event.ConsentGrantedEvent;
import com.pms.patientservice.dto.request.ConsentRequestDTO;
import com.pms.patientservice.grpc.OtpGrpcClient;
import com.pms.patientservice.kafka.ConsentEventProducer;
import com.pms.patientservice.model.Consent;
import com.pms.patientservice.service.ConsentService;
import notification.OptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class ConsentFacade {

    private static final Logger log = LoggerFactory.getLogger(ConsentFacade.class);

    private static final int CONSENT_TTL_SECONDS = 86400;

    private final ConsentService consentService;
    private final OtpGrpcClient otpGrpcClient;
    private final ConsentEventProducer eventProducer;

    public ConsentFacade(ConsentService consentService,
                         OtpGrpcClient otpGrpcClient,
                         ConsentEventProducer eventProducer) {
        this.consentService = consentService;
        this.otpGrpcClient = otpGrpcClient;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public Map<String, Object> requestConsent(ConsentRequestDTO request) {
        log.debug("Requesting consent for patient: {}, doctor: {}, hospital: {}",
                request.patientId(), request.doctorId(), request.hospitalId());

        Consent consent = consentService.createConsent(
                request.patientId(), request.doctorId(), request.hospitalId());

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

        eventProducer.consentGranted(new ConsentGrantedEvent(
                consent.getPatientId(), consent.getHospitalId(), CONSENT_TTL_SECONDS));

        return Map.of(
                "verified", true,
                "status", "ACTIVE",
                "message", "Consent granted"
        );
    }
}
