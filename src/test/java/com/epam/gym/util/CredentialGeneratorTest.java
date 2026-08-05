package com.epam.gym.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialGeneratorTest {

    @Test
    void generateUsername_returnsBaseFormWhenAvailable() {
        String username = CredentialGenerator.generateUsername("John", "Doe", candidate -> false);
        assertEquals("john.doe", username);
    }

    @Test
    void generateUsername_appendsSerialOnCollision() {
        Set<String> taken = Set.of("john.doe", "john.doe1", "john.doe2");
        String username = CredentialGenerator.generateUsername("John", "Doe", taken::contains);
        assertEquals("john.doe3", username);
    }

    @Test
    void generateUsername_lowerCasesAndTrims() {
        String username = CredentialGenerator.generateUsername(" John ", " Doe ", candidate -> false);
        assertEquals("john.doe", username);
    }

    @Test
    void generateUsername_throwsOnBlankFirstName() {
        assertThrows(IllegalArgumentException.class,
                () -> CredentialGenerator.generateUsername("  ", "Doe", candidate -> false));
    }

    @Test
    void generatePassword_hasExpectedLengthAndAlphabet() {
        String password = CredentialGenerator.generatePassword();
        assertEquals(10, password.length());
        assertTrue(password.chars().allMatch(Character::isLetterOrDigit));
    }

    @Test
    void generatePassword_isRandomAcrossCalls() {
        String first = CredentialGenerator.generatePassword();
        String second = CredentialGenerator.generatePassword();
        // Astronomically unlikely to collide; guards against a constant/stub implementation.
        assertTrue(!first.equals(second));
    }
}
