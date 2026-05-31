package com.pms.authservice.grpc;

import hospital.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HospitalGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(HospitalGrpcClient.class);
    private final HospitalServiceGrpc.HospitalServiceBlockingStub blockingStub;
    private final ManagedChannel channel;

    public HospitalGrpcClient(
            @Value("${hospital-service.grpc.host:localhost}") String host,
            @Value("${hospital-service.grpc.port:9003}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = HospitalServiceGrpc.newBlockingStub(channel);
    }

    public DoctorResponse createDoctor(String name, String email, String phone,
                                        String departmentId, String hospitalId) {
        log.debug("Creating doctor via gRPC: email={}, departmentId={}, hospitalId={}", email, departmentId, hospitalId);
        CreateDoctorRequest request = CreateDoctorRequest.newBuilder()
                .setName(name != null ? name : "")
                .setEmail(email != null ? email : "")
                .setPhone(phone != null ? phone : "")
                .setDepartmentId(departmentId != null ? departmentId : "")
                .setHospitalId(hospitalId != null ? hospitalId : "")
                .build();
        return blockingStub.createDoctor(request);
    }

    public List<HospitalResponse> getAllHospitals() {
        log.debug("Fetching all hospitals via gRPC");
        HospitalListResponse response = blockingStub.getAllHospitals(Empty.newBuilder().build());
        return response.getHospitalsList();
    }

    public List<DepartmentResponse> getAllDepartments(String hospitalId) {
        log.debug("Fetching departments for hospitalId: {} via gRPC", hospitalId);
        if (hospitalId == null) return List.of();
        DepartmentByHospitalRequest request = DepartmentByHospitalRequest.newBuilder()
                .setHospitalId(hospitalId)
                .build();
        DepartmentListResponse response = blockingStub.getAllDepartments(request);
        return response.getDepartmentsList();
    }

    public DeleteDoctorResponse deleteDoctor(String doctorId) {
        log.debug("Deleting doctor via gRPC: doctorId={}", doctorId);
        DeleteDoctorRequest request = DeleteDoctorRequest.newBuilder()
                .setDoctorId(doctorId)
                .build();
        return blockingStub.deleteDoctor(request);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Hospital gRPC client");
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}
