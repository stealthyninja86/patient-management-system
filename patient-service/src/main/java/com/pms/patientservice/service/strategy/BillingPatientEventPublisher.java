package com.pms.patientservice.service.strategy;

import com.pms.patientservice.grpc.BillingServiceGrpcClient;
import com.pms.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BillingPatientEventPublisher implements PatientEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BillingPatientEventPublisher.class);

    private final BillingServiceGrpcClient billingClient;

    public BillingPatientEventPublisher(BillingServiceGrpcClient billingClient) {
        this.billingClient = billingClient;
    }

    @Override
    public void publish(Patient patient) {
        log.info("Notifying billing service for patient: {}", patient.getPatientId());
        try {
            billingClient.createBillingAccount(patient.getId().toString(), patient.getName(), patient.getEmail());
        } catch (Exception e) {
            log.error("Failed to notify billing service: {}", e.getMessage());
        }
    }
}
