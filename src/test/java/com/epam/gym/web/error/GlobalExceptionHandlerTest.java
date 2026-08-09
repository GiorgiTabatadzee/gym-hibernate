package com.epam.gym.web.error;

import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.IllegalStateTransitionException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/trainees/john.doe");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void handleValidation_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handleValidation(
                new ValidationException("firstName is required"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("firstName is required", response.getBody().getMessage());
        assertEquals("/api/trainees/john.doe", response.getBody().getPath());
    }

    @Test
    void handleAuthentication_returns401() {
        ResponseEntity<ErrorResponse> response = handler.handleAuthentication(
                new AuthenticationException("Invalid username or password"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new EntityNotFoundException("Trainee not found: john.doe"), request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleStateConflict_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleStateConflict(
                new IllegalStateTransitionException("already active"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleConstraintViolation_returns400WithDetails() {
        ConstraintViolationException e = new ConstraintViolationException("invalid", Set.of());
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(e, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().getValidationErrors());
    }

    @Test
    void handleMissingParam_returns400() {
        MissingServletRequestParameterException e =
                new MissingServletRequestParameterException("username", "String");
        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(e, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleTypeMismatch_returns400() {
        MethodArgumentTypeMismatchException e =
                new MethodArgumentTypeMismatchException("abc", LocalDateStub.class, "periodFrom", null, null);
        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(e, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleUnreadable_returns400() {
        HttpMessageNotReadableException e = mock(HttpMessageNotReadableException.class);
        ResponseEntity<ErrorResponse> response = handler.handleUnreadable(e, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed request body", response.getBody().getMessage());
    }

    @Test
    void handleUnexpected_returns500() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("boom"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    /** Marker type only — MethodArgumentTypeMismatchException just needs *some* target class. */
    private static final class LocalDateStub {
    }
}
