package com.pms.clinicalservice.exception;

public class InvalidPatientPrescriptionOperationException extends RuntimeException{
    public InvalidPatientPrescriptionOperationException(String message) {
        super(message);
    }
}
