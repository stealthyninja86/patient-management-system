package com.pms.authservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import patient.PatientServiceGrpc;
import patient.PatientResponse;

@Component
public class PatientGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcClient.class);
    private final PatientServiceGrpc.PatientServiceBlockingStub blockingStub;
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
                .setName(name)
                .setEmail(email)
                .setPhone(phone)
                .setAddress(address)
                .setDateOfBirth(dateOfBirth)
                .setGender(gender)
                .setBloodType(bloodType)
                .build();
        return blockingStub.createPatient(request);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Patient gRPC client");
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}
