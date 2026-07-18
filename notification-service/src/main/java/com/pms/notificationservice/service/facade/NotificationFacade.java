package com.pms.notificationservice.service.facade;

import com.pms.notificationservice.dto.request.NotificationRequest;
import com.pms.notificationservice.dto.request.OtpGenerateRequestDTO;
import com.pms.notificationservice.dto.response.NotificationResponseDTO;
import com.pms.notificationservice.dto.response.OtpGenerateResponseDTO;
import com.pms.notificationservice.dto.response.OtpVerifyResponseDTO;
import com.pms.notificationservice.service.mapper.NotificationMapper;
import com.pms.notificationservice.service.mapper.OtpMapper;
import com.pms.notificationservice.model.OtpStatus;
import com.pms.notificationservice.service.NotificationService;
import com.pms.notificationservice.service.OtpService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class NotificationFacade {

    private final NotificationService notificationService;
    private final OtpService otpService;
    private final NotificationMapper notificationMapper;
    private final OtpMapper otpMapper;

    public NotificationFacade(NotificationService notificationService,
                              OtpService otpService,
                              NotificationMapper notificationMapper,
                              OtpMapper otpMapper) {
        this.notificationService = notificationService;
        this.otpService = otpService;
        this.notificationMapper = notificationMapper;
        this.otpMapper = otpMapper;
    }

    public boolean sendNotification(NotificationRequest request) {
        return notificationService.sendNotification(notificationMapper.toMessage(request));
    }

    public List<NotificationResponseDTO> getPatientNotificationHistory(String patientId) {
        return notificationService.getPatientNotificationHistory(patientId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public OtpGenerateResponseDTO generateOtp(OtpGenerateRequestDTO request) {
        if (request.domainKey() == null || request.phoneNumber() == null) {
            return new OtpGenerateResponseDTO(null, "domainKey and phoneNumber are required");
        }
        UUID otpId = otpService.generateOtp(request.domainKey(), request.phoneNumber());
        return new OtpGenerateResponseDTO(otpId.toString(), "OTP generated successfully");
    }

    public OtpVerifyResponseDTO verifyOtp(UUID otpId, String code) {
        OtpStatus status = otpService.verifyOtp(otpId, code);
        return otpMapper.toVerifyResponse(status);
    }
}

