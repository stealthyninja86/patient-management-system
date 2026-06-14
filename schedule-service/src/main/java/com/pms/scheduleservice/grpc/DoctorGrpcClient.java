package com.pms.scheduleservice.grpc;

import hospital.DoctorByIdRequest;
import hospital.DoctorResponse;
import hospital.HospitalServiceGrpc;
import hospital.HospitalServiceGrpc.HospitalServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DoctorGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(DoctorGrpcClient.class);

    private final HospitalServiceBlockingStub blockingStub;

    public DoctorGrpcClient(
            @Value("${hospital.service.address}") String host,
            @Value("${hospital.service.grpc.port}") int port) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = HospitalServiceGrpc.newBlockingStub(channel);
    }

    public DoctorResponse getDoctorById(String doctorId) {
        log.debug("Fetching doctor by id: {}", doctorId);
        DoctorByIdRequest request = DoctorByIdRequest.newBuilder()
                .setDoctorId(doctorId)
                .build();
        return blockingStub.getDoctorById(request);
    }
}
