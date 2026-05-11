package com.jpmonitor.platform.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response DTO for all API errors.
 * Provides consistent JSON structure for error messages.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this(status, error, message, path, LocalDateTime.now(), fieldErrors);
    }
}
