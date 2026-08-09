package com.epam.gym.web.security;

import com.epam.gym.exception.AuthenticationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Parses the HTTP Basic {@code Authorization} header used to authenticate every non-public endpoint. */
public final class BasicAuthUtil {

    private static final String BASIC_PREFIX = "Basic ";

    private BasicAuthUtil() {
    }

    public static AuthCredentials parse(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BASIC_PREFIX)) {
            throw new AuthenticationException("Missing or malformed Authorization header; expected HTTP Basic auth");
        }
        String base64Credentials = authorizationHeader.substring(BASIC_PREFIX.length()).trim();
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Malformed Authorization header");
        }
        int separatorIndex = decoded.indexOf(':');
        if (separatorIndex < 0) {
            throw new AuthenticationException("Malformed Authorization header");
        }
        String username = decoded.substring(0, separatorIndex);
        String password = decoded.substring(separatorIndex + 1);
        if (username.isBlank() || password.isEmpty()) {
            throw new AuthenticationException("Username and password must not be blank");
        }
        return new AuthCredentials(username, password);
    }
}
