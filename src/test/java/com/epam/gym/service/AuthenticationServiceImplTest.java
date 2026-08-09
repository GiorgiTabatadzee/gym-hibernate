package com.epam.gym.service;

import com.epam.gym.dao.UserDao;
import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.util.FakeTransactionExecutor;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserDao userDao;

    private AuthenticationServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new AuthenticationServiceImpl(userDao, new FakeTransactionExecutor());
        user = new User("Giorgi", "Beridze", "giorgi.beridze", "Secret123!", true);
        user.setId(1L);
    }

    @Test
    void matchCredentials_returnsTrueOnCorrectPassword() {
        when(userDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(user));
        assertTrue(service.matchCredentials("giorgi.beridze", "Secret123!"));
    }

    @Test
    void matchCredentials_returnsFalseOnWrongPassword() {
        when(userDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(user));
        assertFalse(service.matchCredentials("giorgi.beridze", "wrong"));
    }

    @Test
    void matchCredentials_returnsFalseWhenUsernameUnknown() {
        when(userDao.findByUsername(any(Session.class), eq("nobody"))).thenReturn(Optional.empty());
        assertFalse(service.matchCredentials("nobody", "anything"));
    }

    @Test
    void matchCredentials_returnsFalseOnNullArgs() {
        assertFalse(service.matchCredentials(null, null));
    }

    @Test
    void authenticate_succeedsOnCorrectPassword() {
        when(userDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(user));
        service.authenticate("giorgi.beridze", "Secret123!");
    }

    @Test
    void authenticate_throwsOnWrongPassword() {
        when(userDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(user));
        assertThrows(AuthenticationException.class, () -> service.authenticate("giorgi.beridze", "wrong"));
    }

    @Test
    void authenticate_throwsOnUnknownUsername() {
        when(userDao.findByUsername(any(Session.class), eq("nobody"))).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> service.authenticate("nobody", "anything"));
    }

    @Test
    void authenticate_throwsValidationExceptionOnBlankUsername() {
        assertThrows(ValidationException.class, () -> service.authenticate(" ", "anything"));
    }

    @Test
    void changePassword_updatesPasswordOnCorrectOldPassword() {
        when(userDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(user));
        service.changePassword("giorgi.beridze", "Secret123!", "NewSecret456!");
        assertEquals("NewSecret456!", user.getPassword());
    }

    @Test
    void changePassword_throwsOnWrongOldPassword() {
        when(userDao.findByUsername(any(Session.class), eq("giorgi.beridze"))).thenReturn(Optional.of(user));
        assertThrows(AuthenticationException.class,
                () -> service.changePassword("giorgi.beridze", "wrong", "NewSecret456!"));
    }

    @Test
    void changePassword_throwsValidationExceptionOnBlankNewPassword() {
        assertThrows(ValidationException.class,
                () -> service.changePassword("giorgi.beridze", "Secret123!", " "));
    }
}
