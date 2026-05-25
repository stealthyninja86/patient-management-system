package com.pms.patient_service.strategy.notification;

import com.pms.patient_service.grpc.BillingServiceGrpcClient;
import com.pms.patient_service.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BillingNotificationStrategy implements NotificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(BillingNotificationStrategy.class);

    private final BillingServiceGrpcClient billingClient;

    public BillingNotificationStrategy(BillingServiceGrpcClient billingClient) {
        this.billingClient = billingClient;
    }

    @Override
    public void notify(Patient patient) {
        log.info("Notifying billing service for patient: {}", patient.getPatientId());
        try {
            billingClient.createBillingAccount(patient.getId().toString(), patient.getName(), patient.getEmail());
        } catch (Exception e) {
            log.error("Failed to notify billing service: {}", e.getMessage());
        }
    }
}
