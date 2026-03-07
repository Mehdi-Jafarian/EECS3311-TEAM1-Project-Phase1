package com.platform.domain.exception;

/** Thrown when a booking action is attempted against a consultant who is not yet approved. */
public class ConsultantNotApprovedException extends RuntimeException {
    public ConsultantNotApprovedException(String consultantId) {
        super("Consultant is not approved: " + consultantId);
    }
}
