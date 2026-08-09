package com.epam.gym.web.security;

/** Username/password pair extracted from the HTTP Basic {@code Authorization} header. */
public class AuthCredentials {

    private final String username;
    private final String password;

    public AuthCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        // Never log the password.
        return "AuthCredentials{username='" + username + "', password='***'}";
    }
}
