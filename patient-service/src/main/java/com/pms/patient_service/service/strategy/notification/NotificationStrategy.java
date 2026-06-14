package com.pms.patient_service.service.strategy.notification;

import com.pms.patient_service.model.Patient;

public interface NotificationStrategy {
    void notify(Patient patient);
}
