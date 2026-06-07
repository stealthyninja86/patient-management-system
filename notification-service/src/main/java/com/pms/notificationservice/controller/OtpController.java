package com.pms.notificationservice.controller;

import com.pms.notificationservice.dto.OtpGenerateRequestDTO;
import com.pms.notificationservice.dto.OtpGenerateResponseDTO;
import com.pms.notificationservice.dto.OtpVerifyRequestDTO;
import com.pms.notificationservice.dto.OtpVerifyResponseDTO;
import com.pms.notificationservice.facade.NotificationFacade;
import com.pms.notificationservice.model.OtpStatus;
import com.pms.notificationservice.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/consent/otp")
public class OtpController {

    private final NotificationFacade notificationFacade;
    private final OtpService otpService;

    public OtpController(OtpService otpService, NotificationFacade notificationFacade) {
        this.otpService = otpService;
        this.notificationFacade = notificationFacade;
    }

    @PostMapping("/generate")
    public ResponseEntity<OtpGenerateResponseDTO> generateOtp(
            @RequestBody OtpGenerateRequestDTO request
    ){
        UUID otpId = notificationFacade.generateOtp(request);
        if (otpId == null) {
            return ResponseEntity.badRequest().body(
                new OtpGenerateResponseDTO(null, "patientId, doctorId, and phoneNumber are required"));
        }
        return ResponseEntity.ok(
            new OtpGenerateResponseDTO(otpId.toString(), "OTP generated successfully"));
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpVerifyResponseDTO> verifyOtp(
            @RequestBody OtpVerifyRequestDTO request
    ){
        if (request.otpId() == null || request.code() == null) {
            return ResponseEntity.badRequest().body(
                new OtpVerifyResponseDTO(false, null, "otpId and code are required"));
        }

        OtpStatus status = otpService.verifyOtp(request.otpId(), request.code());
        boolean verified = status == OtpStatus.VERIFIED;

        return ResponseEntity.ok(new OtpVerifyResponseDTO(
            verified, status,
            verified ? "OTP verified successfully" : errorMessage(status)));
    }

    private String errorMessage(OtpStatus status) {
        return switch (status) {
            case EXPIRED -> "OTP has expired. Please request a new one";
            case LOCKED -> "Too many failed attempts. OTP is locked. Please request a new one";
            case GENERATED -> "Incorrect code. Please try again";
            default -> "Verification failed";
        };
    }
}
