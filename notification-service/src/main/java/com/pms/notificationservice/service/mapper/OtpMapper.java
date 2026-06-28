package com.pms.notificationservice.service.mapper;

import com.pms.notificationservice.dto.response.OtpVerifyResponseDTO;
import com.pms.notificationservice.model.OtpStatus;
import org.springframework.stereotype.Component;

@Component
public class OtpMapper {

    public OtpVerifyResponseDTO toVerifyResponse(OtpStatus status, boolean verified) {
        return new OtpVerifyResponseDTO(
            verified,
            status,
            switch (status) {
                case VERIFIED -> "OTP verified successfully";
                case EXPIRED -> "OTP has expired. Please request a new one";
                case LOCKED -> "Too many failed attempts. OTP is locked. Please request a new one";
                case GENERATED -> "Incorrect code. Please try again";
            }
        );
    }

    public OtpVerifyResponseDTO toVerifyResponse(OtpStatus status) {
        return toVerifyResponse(status, status == OtpStatus.VERIFIED);
    }
}
