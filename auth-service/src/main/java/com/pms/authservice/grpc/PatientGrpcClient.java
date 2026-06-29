package com.pms.authservice.grpc;

import com.pms.authservice.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import patient.PatientDeleteRequest;
import patient.PatientDeleteResponse;
import patient.PatientResponse;

@Component
public class PatientGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcClient.class);

    @GrpcClient("patient-service")
    private patient.PatientServiceGrpc.PatientServiceBlockingStub blockingStub;

    @CircuitBreaker(name = "patientService", fallbackMethod = "createPatientFallback")
    public PatientResponse createPatient(String name, String email, String phone,
                                          String address, String dateOfBirth,
                                          String gender, String bloodType) {
        log.debug("Creating patient via gRPC: email={}", email);
        patient.CreatePatientRequest request = patient.CreatePatientRequest.newBuilder()
                .setName(name != null ? name : "")
                .setEmail(email != null ? email : "")
                .setPhone(phone != null ? phone : "")
                .setAddress(address != null ? address : "")
                .setDateOfBirth(dateOfBirth != null ? dateOfBirth : "")
                .setGender(gender != null ? gender : "")
                .setBloodType(bloodType != null ? bloodType : "")
                .build();
        return blockingStub.createPatient(request);
    }

    private PatientResponse createPatientFallback(String name, String email, String phone,
                                                   String address, String dateOfBirth,
                                                   String gender, String bloodType,
                                                   Throwable t) {
        log.error("Circuit BREAK OPEN for patientService (createPatient). Blocking registration. email: {}, error: {}",
                email, t.getMessage());
        throw new ServiceUnavailableException("Patient Service is currently unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "patientService", fallbackMethod = "deletePatientFallback")
    public PatientDeleteResponse deletePatient(String patientId) {
        PatientDeleteRequest request = PatientDeleteRequest.newBuilder().
                setPatientId(patientId)
                .build();
        log.debug("Deleting patient via gRPC: request={}", request);
        return blockingStub.deletePatient(request);
    }

    private PatientDeleteResponse deletePatientFallback(String patientId, Throwable t) {
        log.warn("Circuit BREAK OPEN for patientService (deletePatient). Saga compensation will fail. patientId: {}, error: {}",
                patientId, t.getMessage());
        throw new ServiceUnavailableException("Patient Service is currently unavailable. Compensation failed.");
    }
}
