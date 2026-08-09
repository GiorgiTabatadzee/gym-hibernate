package com.epam.gym.web.error;

import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.EntityNotFoundException;
import com.epam.gym.exception.IllegalStateTransitionException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static com.epam.gym.web.logging.TransactionIdFilter.MDC_KEY;

/**
 * Central error handling (task notes #15/#18): every endpoint funnels its failures through here,
 * so callers always get the same {@link ErrorResponse} shape with an HTTP status that matches the
 * failure kind, and every failure is logged with the request's transactionId.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, e.getMessage(), request, null);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), request, null);
    }

    @ExceptionHandler(IllegalStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleStateConflict(IllegalStateTransitionException e,
                                                               HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, e.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException e,
                                                                HttpServletRequest request) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e,
                                                                     HttpServletRequest request) {
        List<String> details = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e,
                                                              HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                              HttpServletRequest request) {
        String message = "Invalid value for parameter '" + e.getName() + "'";
        return build(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e,
                                                            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception while processing {} {}", request.getMethod(), request.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request,
                                                 List<String> validationErrors) {
        ErrorResponse body = new ErrorResponse();
        body.setStatus(status.value());
        body.setError(status.getReasonPhrase());
        body.setMessage(message);
        body.setPath(request.getRequestURI());
        body.setTransactionId(MDC.get(MDC_KEY));
        body.setValidationErrors(validationErrors);
        if (status.is5xxServerError()) {
            log.error("{} {} -> {} {}", request.getMethod(), request.getRequestURI(), status.value(), message);
        } else {
            log.warn("{} {} -> {} {}", request.getMethod(), request.getRequestURI(), status.value(), message);
        }
        return ResponseEntity.status(status).body(body);
    }
}
