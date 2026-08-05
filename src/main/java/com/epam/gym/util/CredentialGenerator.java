package com.epam.gym.util;

import java.security.SecureRandom;
import java.util.function.Predicate;

/**
 * Generates login usernames and passwords for new Trainee/Trainer profiles.
 *
 * NOTE: this reimplements the scheme in a standard, commonly-used way since the
 * original "previous module" generator wasn't available to reference here:
 *   - username = "firstname.lastname" (lower-cased); on collision, append the
 *     smallest positive serial suffix that is not already taken (john.doe1, ...)
 *   - password = random 10-character alphanumeric string
 * Swap the implementation here if your previous module used a different rule —
 * every caller goes through this one class.
 */
public final class CredentialGenerator {

    private static final String PASSWORD_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private CredentialGenerator() {
    }

    /**
     * @param usernameExists predicate returning true if the candidate username is already taken
     */
    public static String generateUsername(String firstName, String lastName, Predicate<String> usernameExists) {
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("firstName and lastName are required to generate a username");
        }
        String base = (firstName.trim() + "." + lastName.trim()).toLowerCase();
        if (!usernameExists.test(base)) {
            return base;
        }
        int serial = 1;
        String candidate;
        do {
            candidate = base + serial;
            serial++;
        } while (usernameExists.test(candidate));
        return candidate;
    }

    public static String generatePassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_ALPHABET.charAt(RANDOM.nextInt(PASSWORD_ALPHABET.length())));
        }
        return sb.toString();
    }
}
