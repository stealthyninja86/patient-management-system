package com.pms.patient_service.grpc;

import com.pms.patient_service.dto.PatientGrpcRequestDTO;
import com.pms.patient_service.dto.PatientResponseDTO;
import com.pms.patient_service.facade.PatientFacade;
import patient.CreatePatientRequest;
import patient.PatientByIdRequest;
import patient.PatientResponse;
import patient.PatientServiceGrpc.PatientServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class PatientGrpcService extends PatientServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcService.class);
    private final PatientFacade patientFacade;

    public PatientGrpcService(PatientFacade patientFacade) {
        this.patientFacade = patientFacade;
    }

    @Override
    public void getPatientById(PatientByIdRequest request, StreamObserver<PatientResponse> responseObserver) {
        log.info("gRPC request to get patient by patientId: {}", request.getPatientId());
        try {
            PatientResponseDTO dto = patientFacade.getPatientByPatientId(request.getPatientId());
            PatientResponse response = PatientResponse.newBuilder()
                    .setId(dto.id())
                    .setPatientId(dto.patientId())
                    .setName(dto.name() != null ? dto.name() : "")
                    .setEmail(dto.email() != null ? dto.email() : "")
                    .setAddress(dto.address() != null ? dto.address() : "")
                    .setDateOfBirth(dto.dateOfBirth() != null ? dto.dateOfBirth() : "")
                    .setPhone(dto.phone() != null ? dto.phone() : "")
                    .setGender(dto.gender() != null ? dto.gender() : "")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting patient: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void createPatient(CreatePatientRequest request, StreamObserver<PatientResponse> responseObserver) {
        log.info("gRPC request to create patient with email: {}", request.getEmail());
        try {
            PatientGrpcRequestDTO dto = new PatientGrpcRequestDTO(
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getAddress(),
                    request.getDateOfBirth(),
                    request.getGender(),
                    request.getBloodType(),
                    request.getRegisteredDate()
            );
            PatientResponseDTO created = patientFacade.createPatient(dto);
            PatientResponse response = PatientResponse.newBuilder()
                    .setId(created.id())
                    .setPatientId(created.patientId())
                    .setName(created.name() != null ? created.name() : "")
                    .setEmail(created.email() != null ? created.email() : "")
                    .setAddress(created.address() != null ? created.address() : "")
                    .setDateOfBirth(created.dateOfBirth() != null ? created.dateOfBirth() : "")
                    .setPhone(created.phone() != null ? created.phone() : "")
                    .setGender(created.gender() != null ? created.gender() : "")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating patient: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }
}
