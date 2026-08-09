package com.epam.gym.web.dto;

import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;
import java.util.List;

/** Uniform error body returned by every endpoint on failure. */
public class ErrorResponse {

    @ApiModelProperty(value = "When the error occurred")
    private final Instant timestamp = Instant.now();

    @ApiModelProperty(value = "HTTP status code", example = "400")
    private int status;

    @ApiModelProperty(value = "HTTP status reason phrase", example = "Bad Request")
    private String error;

    @ApiModelProperty(value = "Human-readable error message")
    private String message;

    @ApiModelProperty(value = "Request path that failed")
    private String path;

    @ApiModelProperty(value = "Transaction id — correlate with server logs / downstream services")
    private String transactionId;

    @ApiModelProperty(value = "Field-level validation errors, if any")
    private List<String> validationErrors;

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
