package com.pms.authservice.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import patient.PatientDeleteRequest;
import patient.PatientDeleteResponse;
import patient.PatientServiceGrpc;
import patient.PatientServiceGrpc.PatientServiceBlockingStub;
import patient.PatientResponse;

@Component
public class PatientGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcClient.class);

    @GrpcClient("patient-service")
    private PatientServiceBlockingStub blockingStub;

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
}
