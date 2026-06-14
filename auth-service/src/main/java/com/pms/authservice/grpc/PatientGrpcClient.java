package com.pms.authservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import patient.PatientDeleteRequest;
import patient.PatientDeleteResponse;
import patient.PatientServiceGrpc;
import patient.PatientServiceGrpc.PatientServiceBlockingStub;
import patient.PatientResponse;

@Component
public class PatientGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcClient.class);
    private final PatientServiceBlockingStub blockingStub;
    private final ManagedChannel channel;

    public PatientGrpcClient(
            @Value("${patient-service.grpc.host:localhost}") String host,
            @Value("${patient-service.grpc.port:9000}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = PatientServiceGrpc.newBlockingStub(channel);
    }

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

    public PatientDeleteResponse deletePatient(String patientId) {
        PatientDeleteRequest request = PatientDeleteRequest.newBuilder().
                setPatientId(patientId)
                .build();
        log.debug("Deleting patient via gRPC: request={}", request);
        return blockingStub.deletePatient(request);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Patient gRPC client");
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}
