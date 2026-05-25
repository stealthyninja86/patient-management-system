package com.pms.clinicalservice.exception;

public class InvalidPrescriptionOperationException extends RuntimeException {
    public InvalidPrescriptionOperationException(String message) {
        super(message);
    }
}
