package com.jpmonitor.platform.util;

import java.util.UUID;

/**
 * Utility class for UUID ↔ String conversions
 * Provides consistent error handling across the application
 */
public final class UUIDConverter {

    private UUIDConverter() {
        // Utility class, no instantiation
    }

    /**
     * Convert String to UUID with validation
     * 
     * @param id String representation of UUID
     * @return UUID object
     * @throws IllegalArgumentException if string is invalid UUID format
     */
    public static UUID toUUID(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        try {
            return UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + id, e);
        }
    }

    /**
     * Convert UUID to String
     * 
     * @param uuid UUID object
     * @return String representation
     */
    public static String toString(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    /**
     * Safely convert String to UUID, returning null if invalid
     * 
     * @param id String representation
     * @return UUID or null if invalid
     */
    public static UUID toUUIDSafe(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
