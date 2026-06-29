package com.pms.clinicalservice.grpc;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
        log.warn("Circuit breaker OPEN for patientService. Patient ID: {} , error: {}", patientId, throwable.getMessage());
        return PatientResponse.newBuilder()
                .setPatientId(patientId)
                .build();
    }
}
