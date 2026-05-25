package com.pms.clinicalservice.grpc;

import hospital.DepartmentByIdRequest;
import hospital.DepartmentResponse;
import hospital.DoctorByIdRequest;
import hospital.DoctorResponse;
import hospital.HospitalServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HospitalGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(HospitalGrpcClient.class);

    private final HospitalServiceGrpc.HospitalServiceBlockingStub blockingStub;

    public HospitalGrpcClient(
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

    public DepartmentResponse getDepartmentById(String departmentId) {
        log.debug("Fetching department by id: {}", departmentId);
        DepartmentByIdRequest request = DepartmentByIdRequest.newBuilder()
                .setDepartmentId(departmentId)
                .build();
        return blockingStub.getDepartmentById(request);
    }
}
