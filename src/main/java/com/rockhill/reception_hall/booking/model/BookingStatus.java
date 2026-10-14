package com.rockhill.reception_hall.booking.model;

import java.util.EnumSet;
import java.util.Set;

/** State pattern: each status knows which statuses it may move to. */
public enum BookingStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED;

    public Set<BookingStatus> allowedNext() {
        return switch (this) {
            case PENDING   -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(COMPLETED, CANCELLED, PENDING); // PENDING = customer reschedule awaiting approval
            case COMPLETED, CANCELLED -> EnumSet.noneOf(BookingStatus.class);
        };
    }

    public boolean canMoveTo(BookingStatus next) {
        return allowedNext().contains(next);
    }
}