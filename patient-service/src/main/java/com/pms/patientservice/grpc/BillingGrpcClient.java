package com.pms.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceBlockingStub;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BillingGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcClient.class);
    //localhost:9001/BillingService/CreatePatientService
    //aws.grpc:123123/BillingService/CreatePatientService
    @GrpcClient("billing-service")
    private BillingServiceBlockingStub blockingStub;

    public BillingResponse createBillingAccount(String PatientId, String name, String email){
        BillingRequest request = BillingRequest
                .newBuilder()
                .setPatientId(PatientId)
                .setName(name)
                .setEmail(email)
                .build();

        BillingResponse response = blockingStub.createBillingAccount(request);
        log.info("Received response from billing service: {}", response);
        return response;
    }

}
