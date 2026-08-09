package com.epam.gym.web.security;

import com.epam.gym.exception.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BasicAuthUtilTest {

    @Test
    void parse_decodesValidHeader() {
        String header = "Basic " + Base64.getEncoder().encodeToString("john.doe:Secret123!".getBytes());
        AuthCredentials credentials = BasicAuthUtil.parse(header);
        assertEquals("john.doe", credentials.getUsername());
        assertEquals("Secret123!", credentials.getPassword());
    }

    @Test
    void parse_throwsOnNullHeader() {
        assertThrows(AuthenticationException.class, () -> BasicAuthUtil.parse(null));
    }

    @Test
    void parse_throwsOnNonBasicScheme() {
        assertThrows(AuthenticationException.class, () -> BasicAuthUtil.parse("Bearer abc123"));
    }

    @Test
    void parse_throwsOnInvalidBase64() {
        assertThrows(AuthenticationException.class, () -> BasicAuthUtil.parse("Basic not-base64!!"));
    }

    @Test
    void parse_throwsWhenNoColonSeparator() {
        String header = "Basic " + Base64.getEncoder().encodeToString("nocolonhere".getBytes());
        assertThrows(AuthenticationException.class, () -> BasicAuthUtil.parse(header));
    }

    @Test
    void parse_throwsOnBlankUsername() {
        String header = "Basic " + Base64.getEncoder().encodeToString(":Secret123!".getBytes());
        assertThrows(AuthenticationException.class, () -> BasicAuthUtil.parse(header));
    }

    @Test
    void parse_throwsOnEmptyPassword() {
        String header = "Basic " + Base64.getEncoder().encodeToString("john.doe:".getBytes());
        assertThrows(AuthenticationException.class, () -> BasicAuthUtil.parse(header));
    }

    @Test
    void parse_allowsColonInsidePassword() {
        String header = "Basic " + Base64.getEncoder().encodeToString("john.doe:pass:word".getBytes());
        AuthCredentials credentials = BasicAuthUtil.parse(header);
        assertEquals("pass:word", credentials.getPassword());
    }
}
