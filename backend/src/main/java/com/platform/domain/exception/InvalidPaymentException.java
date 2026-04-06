package com.platform.domain.exception;

/** Thrown when payment details fail validation. */
public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
