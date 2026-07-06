package com.pms.notificationservice.exception;

public class OtpCoolDownException extends RuntimeException {
    public OtpCoolDownException(String message) {
        super(message);
    }
}
