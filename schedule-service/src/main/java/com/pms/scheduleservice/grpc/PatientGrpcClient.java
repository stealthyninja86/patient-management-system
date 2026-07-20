package com.pms.scheduleservice.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import patient.ConsentCheckRequest;
import patient.ConsentCheckResponse;
import patient.PatientByIdRequest;
import patient.PatientResponse;
import patient.PatientServiceGrpc;

@Component
public class PatientGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcClient.class);

    @GrpcClient("patient-service")
    private PatientServiceGrpc.PatientServiceBlockingStub blockingStub;

    public PatientResponse getPatientById(String patientId) {
        log.debug("Fetching patient by id: {}", patientId);
        PatientByIdRequest request = PatientByIdRequest.newBuilder()
                .setPatientId(patientId)
                .build();
        return blockingStub.getPatientById(request);
    }

    public boolean checkConsent(String patientId, String hospitalId) {
        log.debug("Checking consent for patient: {}, hospital: {}", patientId, hospitalId);
        ConsentCheckRequest request = ConsentCheckRequest.newBuilder()
                .setPatientId(patientId)
                .setHospitalId(hospitalId)
                .build();
        ConsentCheckResponse response = blockingStub.checkConsent(request);
        return response.getHasConsent();
    }
}
