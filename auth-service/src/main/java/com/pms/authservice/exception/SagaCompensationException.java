package com.pms.authservice.exception;

public class SagaCompensationException extends RuntimeException {
    public SagaCompensationException(String message, Throwable cause) {
        super(message, cause);
    }
}
