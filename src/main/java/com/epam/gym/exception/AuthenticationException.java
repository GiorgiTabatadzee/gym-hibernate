package com.epam.gym.exception;

/** Thrown when a username/password pair does not match, or the account is inactive. */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
