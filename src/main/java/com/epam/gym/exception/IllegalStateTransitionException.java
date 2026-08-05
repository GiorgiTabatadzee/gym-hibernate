package com.epam.gym.exception;

/**
 * Thrown by activate/de-activate operations, which are explicitly NOT idempotent
 * (task note #6): calling activate on an already-active profile is an error,
 * not a silent no-op, and likewise for de-activate.
 */
public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String message) {
        super(message);
    }
}
