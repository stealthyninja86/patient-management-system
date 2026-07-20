package com.pms.clinicalservice.grpc;

import com.pms.clinicalservice.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import patient.ConsentCheckRequest;
import patient.ConsentCheckResponse;
import patient.PatientByIdRequest;
import patient.PatientResponse;
import patient.PatientServiceGrpc;

@Component
public class PatientGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcClient.class);

    @GrpcClient("patient-service")
    private  PatientServiceGrpc.PatientServiceBlockingStub blockingStub;

    @CircuitBreaker(name = "patientService", fallbackMethod = "getPatientByIdFallback")
    public PatientResponse getPatientById(String patientId) {
        log.debug("Fetching patient by id: {}", patientId);
        PatientByIdRequest request = PatientByIdRequest.newBuilder()
                .setPatientId(patientId)
                .build();
        return blockingStub.getPatientById(request);
    }

    private PatientResponse getPatientByIdFallback(String patientId, Throwable throwable) {
        log.error("Circuit breaker OPEN for patientService. Patient ID: {} , error: {}", patientId, throwable.getMessage());
        throw new ServiceUnavailableException("Patient service is currently unavailable. Please try again.");
    }

    @CircuitBreaker(name = "patientService", fallbackMethod = "checkConsentFallback")
    public boolean checkConsent(String patientId, String hospitalId) {
        log.debug("Checking consent for patient: {}, hospital: {}", patientId, hospitalId);
        ConsentCheckRequest request = ConsentCheckRequest.newBuilder()
                .setPatientId(patientId)
                .setHospitalId(hospitalId)
                .build();
        ConsentCheckResponse response = blockingStub.checkConsent(request);
        return response.getHasConsent();
    }

    private boolean checkConsentFallback(String patientId, String hospitalId, Throwable throwable) {
        log.error("Circuit breaker OPEN for patientService consent check. Patient: {}, hospital: {}, error: {}",
                patientId, hospitalId, throwable.getMessage());
        return false;
    }
}
