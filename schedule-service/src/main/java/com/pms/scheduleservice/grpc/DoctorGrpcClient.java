package com.pms.scheduleservice.grpc;

import hospital.DoctorByIdRequest;
import hospital.DoctorResponse;
import hospital.HospitalServiceGrpc;
import hospital.HospitalServiceGrpc.HospitalServiceBlockingStub;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DoctorGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(DoctorGrpcClient.class);

    @GrpcClient("hospital-service")
    private HospitalServiceBlockingStub blockingStub;

    public DoctorResponse getDoctorById(String doctorId) {
        log.debug("Fetching doctor by id: {}", doctorId);
        DoctorByIdRequest request = DoctorByIdRequest.newBuilder()
                .setDoctorId(doctorId)
                .build();
        return blockingStub.getDoctorById(request);
    }
}
