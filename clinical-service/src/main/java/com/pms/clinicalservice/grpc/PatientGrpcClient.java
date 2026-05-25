package com.pms.clinicalservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import patient.PatientByIdRequest;
import patient.PatientResponse;
import patient.PatientServiceGrpc;

@Component
public class PatientGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcClient.class);

    private final PatientServiceGrpc.PatientServiceBlockingStub blockingStub;

    public PatientGrpcClient(
            @Value("${patient.service.address}") String host,
            @Value("${patient.service.grpc.port}") int port) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = PatientServiceGrpc.newBlockingStub(channel);
    }

    public PatientResponse getPatientById(String patientId) {
        log.debug("Fetching patient by id: {}", patientId);
        PatientByIdRequest request = PatientByIdRequest.newBuilder()
                .setPatientId(patientId)
                .build();
        return blockingStub.getPatientById(request);
    }
}
