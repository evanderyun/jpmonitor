package com.jpm.erp.platform.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when request validation fails.
 * Results in HTTP 400 Bad Request response with field errors.
 */
public class ValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
