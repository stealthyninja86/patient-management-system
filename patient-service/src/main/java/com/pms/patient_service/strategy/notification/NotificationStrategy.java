package com.pms.patient_service.strategy.notification;

import com.pms.patient_service.model.Patient;

public interface NotificationStrategy {
    void notify(Patient patient);
}
