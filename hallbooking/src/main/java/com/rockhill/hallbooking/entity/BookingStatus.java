package com.rockhill.hallbooking.entity;

/** Booking.status ENUM('Pending','Confirmed','Completed','Cancelled'). */
public enum BookingStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String dbValue;

    BookingStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static BookingStatus fromDbValue(String value) {
        for (BookingStatus s : values()) {
            if (s.dbValue.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown booking status: " + value);
    }
}
