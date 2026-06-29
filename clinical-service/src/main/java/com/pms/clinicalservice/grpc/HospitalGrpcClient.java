package com.pms.clinicalservice.grpc;

import hospital.DepartmentByIdRequest;
import hospital.DepartmentResponse;
import hospital.DoctorByIdRequest;
import hospital.DoctorResponse;
import hospital.HospitalByIdRequest;
import hospital.HospitalResponse;
import hospital.HospitalServiceGrpc;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HospitalGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(HospitalGrpcClient.class);

    @GrpcClient("hospital-service")
    private HospitalServiceGrpc.HospitalServiceBlockingStub blockingStub;

    @CircuitBreaker(name = "hospitalService", fallbackMethod = "getDoctorByIdFallback")
    public DoctorResponse getDoctorById(String doctorId) {
        log.debug("Fetching doctor by id: {}", doctorId);
        DoctorByIdRequest request = DoctorByIdRequest.newBuilder()
                .setDoctorId(doctorId)
                .build();
        return blockingStub.getDoctorById(request);
    }

    private DoctorResponse getDoctorByIdFallback(String doctorId, Throwable t) {
        log.warn("Circuit BREAK OPEN for hospitalService (getDoctorById). Returning degraded response. doctorId: {}, error: {}", doctorId, t.getMessage());
        return DoctorResponse.newBuilder()
                .setDoctorId(doctorId)
                .setName("Unavailable - Circuit Open")
                .build();
    }

    @CircuitBreaker(name = "hospitalService", fallbackMethod = "getDepartmentByIdFallback")
    public DepartmentResponse getDepartmentById(String departmentId) {
        log.debug("Fetching department by id: {}", departmentId);
        DepartmentByIdRequest request = DepartmentByIdRequest.newBuilder()
                .setDepartmentId(departmentId)
                .build();
        return blockingStub.getDepartmentById(request);
    }

    private DepartmentResponse getDepartmentByIdFallback(String departmentId, Throwable t) {
        log.warn("Circuit BREAK OPEN for hospitalService (getDepartmentById). Returning degraded response. departmentId: {}, error: {}", departmentId, t.getMessage());
        return DepartmentResponse.newBuilder()
                .setDepartmentId(departmentId)
                .setName("Unavailable - Circuit Open")
                .build();
    }

    @CircuitBreaker(name = "hospitalService", fallbackMethod = "getHospitalByIdFallback")
    public HospitalResponse getHospitalById(String hospitalId) {
        log.debug("Fetching hospital by id: {}", hospitalId);
        HospitalByIdRequest request = HospitalByIdRequest.newBuilder()
                .setHospitalId(hospitalId)
                .build();
        return blockingStub.getHospitalById(request);
    }

    private HospitalResponse getHospitalByIdFallback(String hospitalId, Throwable t) {
        log.warn("Circuit BREAK OPEN for hospitalService (getHospitalById). Returning degraded response. hospitalId: {}, error: {}", hospitalId, t.getMessage());
        return HospitalResponse.newBuilder()
                .setHospitalId(hospitalId)
                .setName("Unavailable - Circuit Open")
                .build();
    }
}
