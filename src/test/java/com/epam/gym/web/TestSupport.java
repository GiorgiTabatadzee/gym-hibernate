package com.epam.gym.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class TestSupport {

    private TestSupport() {
    }

    public static String basicAuthHeader(String username, String password) {
        String raw = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
