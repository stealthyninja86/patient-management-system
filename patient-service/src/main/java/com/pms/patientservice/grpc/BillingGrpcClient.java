package com.pms.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceBlockingStub;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BillingGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcClient.class);

    @GrpcClient("billing-service")
    private BillingServiceBlockingStub blockingStub;

    @CircuitBreaker(name = "billingService", fallbackMethod = "createBillingAccountFallback")
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

    private BillingResponse createBillingAccountFallback(String PatientId, String name, String email, Throwable t) {
        log.warn("Circuit BREAK OPEN for billingService (createBillingAccount). Returning deferred response. patientId: {}, error: {}",
                PatientId, t.getMessage());
        return BillingResponse.newBuilder()
                .setAccountId("")
                .setStatus("DEFERRED")
                .build();
    }

}
