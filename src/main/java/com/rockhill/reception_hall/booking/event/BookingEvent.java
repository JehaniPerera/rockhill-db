package com.rockhill.reception_hall.booking.event;

import com.rockhill.reception_hall.booking.model.Booking;

/** Observer pattern: published by BookingService, handled by listeners. */
public record BookingEvent(Booking booking, Type type) {
    public enum Type { SUBMITTED, CONFIRMED, REJECTED, RESCHEDULE_REQUESTED, RESCHEDULED, CANCELLED, COMPLETED, DEPOSIT_OVERDUE }
}