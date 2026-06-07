package com.pms.notificationservice.facade;


import com.pms.notificationservice.dto.NotificationRequest;
import com.pms.notificationservice.dto.OtpGenerateRequestDTO;
import com.pms.notificationservice.model.Notification;
import com.pms.notificationservice.model.OtpStatus;
import com.pms.notificationservice.service.NotificationService;
import com.pms.notificationservice.service.OtpService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * provides a unified interface over NotificationService + OtpService.
 */
@Service
public class NotificationFacade {

    private final NotificationService notificationService;
    private final OtpService otpService;

    public NotificationFacade(NotificationService notificationService, OtpService otpService) {
        this.notificationService = notificationService;
        this.otpService = otpService;
    }

    public boolean sendNotification(NotificationRequest request) {
        return notificationService.sendNotification(request);
    }

    public List<Notification> getPatientNotificationHistory(String patientId) {
        return notificationService.getPatientNotificationHistory(patientId);
    }

    public UUID generateOtp(OtpGenerateRequestDTO request) {
        if(request.patientId() == null || request.doctorId() == null || request.phoneNumber() == null){
            return null;
        }
        return otpService.generateOtp(request.patientId(), request.doctorId(),
                request.hospitalId(), request.consentRequestId(), request.phoneNumber());
    }

    public OtpStatus verifyOtp(UUID otpId, String code) {
        return otpService.verifyOtp(otpId, code);
    }
}

