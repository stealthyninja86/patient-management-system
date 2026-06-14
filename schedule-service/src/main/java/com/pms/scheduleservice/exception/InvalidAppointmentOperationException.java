package com.pms.scheduleservice.exception;

public class InvalidAppointmentOperationException extends RuntimeException {
    public InvalidAppointmentOperationException(String message) {
        super(message);
    }
}
