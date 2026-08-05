package com.epam.gym.exception;

/** Thrown when required-field or business-rule validation fails before a create/update. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
