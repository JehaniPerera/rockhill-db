package com.rockhill.reception_hall.menu;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class CatalogValidation {

    private CatalogValidation() {}

    static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw bad(label + " is required.");
        }
        return value.trim();
    }

    static double requireNonNegative(Double value, String label) {
        if (value == null) {
            throw bad(label + " is required.");
        }
        if (value.isNaN() || value.isInfinite()) {
            throw bad(label + " must be a valid number.");
        }
        if (value < 0) {
            throw bad(label + " cannot be negative.");
        }
        return value;
    }

    static boolean sameName(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }
}
