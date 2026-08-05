package com.epam.gym.exception;

/** Thrown when a lookup by id/username finds no matching row. */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
