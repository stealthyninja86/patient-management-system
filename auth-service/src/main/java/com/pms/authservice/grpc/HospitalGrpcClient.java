package com.pms.authservice.grpc;

import com.pms.authservice.exception.ServiceUnavailableException;
import hospital.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import hospital.HospitalServiceGrpc.HospitalServiceBlockingStub;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HospitalGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(HospitalGrpcClient.class);

    @GrpcClient("hospital-service")
    private HospitalServiceBlockingStub blockingStub;

    @CircuitBreaker(name = "hospitalService", fallbackMethod = "createDoctorFallback")
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

    public DoctorResponse createDoctorFallback(String name, String email, String phone,
                                                String departmentId, String hospitalId,
                                                Throwable throwable) {
        log.error("Circuit BREAK OPEN for hospitalService (createDoctor). Blocking registration. email: {}, error: {}",
                email, throwable.getMessage());
        throw new ServiceUnavailableException("Hospital Service is currently unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "hospitalService", fallbackMethod = "getAllHospitalsFallback")
    public List<HospitalResponse> getAllHospitals() {
        log.debug("Fetching all hospitals via gRPC");
        HospitalListResponse response = blockingStub.getAllHospitals(Empty.newBuilder().build());
        return response.getHospitalsList();
    }

    private List<HospitalResponse> getAllHospitalsFallback(Throwable t) {
        log.warn("Circuit BREAK OPEN for hospitalService (getAllHospitals). Returning empty list. error: {}", t.getMessage());
        return List.of();
    }

    @CircuitBreaker(name = "hospitalService", fallbackMethod = "getAllDepartmentsFallback")
    public List<DepartmentResponse> getAllDepartments(String hospitalId) {
        log.debug("Fetching departments for hospitalId: {} via gRPC", hospitalId);
        if (hospitalId == null) return List.of();
        DepartmentByHospitalRequest request = DepartmentByHospitalRequest.newBuilder()
                .setHospitalId(hospitalId)
                .build();
        DepartmentListResponse response = blockingStub.getAllDepartments(request);
        return response.getDepartmentsList();
    }

    private List<DepartmentResponse> getAllDepartmentsFallback(String hospitalId, Throwable t) {
        log.warn("Circuit BREAK OPEN for hospitalService (getAllDepartments). Returning empty list. hospitalId: {}, error: {}", hospitalId, t.getMessage());
        return List.of();
    }

    @CircuitBreaker(name = "hospitalService", fallbackMethod = "deleteDoctorFallback")
    public DeleteDoctorResponse deleteDoctor(String doctorId) {
        log.debug("Deleting doctor via gRPC: doctorId={}", doctorId);
        DeleteDoctorRequest request = DeleteDoctorRequest.newBuilder()
                .setDoctorId(doctorId)
                .build();
        return blockingStub.deleteDoctor(request);
    }

    private DeleteDoctorResponse deleteDoctorFallback(String doctorId, Throwable t) {
        log.warn("Circuit BREAK OPEN for hospitalService (deleteDoctor). Saga compensation will fail. doctorId: {}, error: {}", doctorId, t.getMessage());
        throw new ServiceUnavailableException("Hospital Service is currently unavailable. Compensation failed.");
    }

}
