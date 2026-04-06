package com.platform.domain.exception;

/** Thrown when a client or consultant with the given ID cannot be found. */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
