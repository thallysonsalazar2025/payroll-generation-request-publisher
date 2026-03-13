package com.example.payrollpublisher.logging;

/**
 * Utility to avoid leaking sensitive identifiers in logs.
 * Shows only the last few characters of a string and replaces the rest with asterisks.
 */
public final class SensitiveDataSanitizer {

    private static final int VISIBLE_TAIL = 3;

    private SensitiveDataSanitizer() {
    }

    public static String mask(String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }

        int visible = Math.min(VISIBLE_TAIL, value.length());
        int masked = Math.max(0, value.length() - visible);
        return "*".repeat(masked) + value.substring(value.length() - visible);
    }
}
