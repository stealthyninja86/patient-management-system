package com.pms.notificationservice.controller;

import com.pms.notificationservice.dto.request.OtpGenerateRequestDTO;
import com.pms.notificationservice.dto.response.OtpGenerateResponseDTO;
import com.pms.notificationservice.dto.request.OtpVerifyRequestDTO;
import com.pms.notificationservice.dto.response.OtpVerifyResponseDTO;
import com.pms.notificationservice.service.facade.NotificationFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consent/otp")
public class OtpController {

    private final NotificationFacade notificationFacade;

    public OtpController(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @PostMapping("/generate")
    public ResponseEntity<OtpGenerateResponseDTO> generateOtp(
            @RequestBody OtpGenerateRequestDTO request
    ){
        OtpGenerateResponseDTO response = notificationFacade.generateOtp(request);
        if (response.otpId() == null) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpVerifyResponseDTO> verifyOtp(
            @RequestBody OtpVerifyRequestDTO request
    ){
        if (request.otpId() == null || request.code() == null) {
            return ResponseEntity.badRequest().body(
                new OtpVerifyResponseDTO(false, null, "otpId and code are required"));
        }
        return ResponseEntity.ok(notificationFacade.verifyOtp(request.otpId(), request.code()));
    }
}
