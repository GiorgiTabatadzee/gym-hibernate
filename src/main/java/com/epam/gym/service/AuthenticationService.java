package com.epam.gym.service;

/**
 * User-type-agnostic credential check against the Users table. Trainee/Trainer specific
 * operations authenticate through their own service (which also verifies the account is the
 * right kind of profile); this is for endpoints that only need "is this a valid username/password
 * pair" — Login (#3) and cross-cutting request authentication.
 */
public interface AuthenticationService {

    /** #3 Username/password matching, regardless of whether the account is a trainee or trainer. */
    boolean matchCredentials(String username, String password);

    /** Throws AuthenticationException if the pair does not match a persisted user. */
    void authenticate(String username, String password);

    /** #4 Change Login: verifies oldPassword (this check IS the authentication for this call) then updates it. */
    void changePassword(String username, String oldPassword, String newPassword);
}
