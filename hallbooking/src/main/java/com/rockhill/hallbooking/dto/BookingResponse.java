package com.rockhill.hallbooking.dto;

import com.rockhill.hallbooking.entity.Booking;

import java.time.LocalDate;

public record BookingResponse(Long bookingId, LocalDate eventDate, String status) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getBookingId(), b.getEventDate(), b.getStatus().getDbValue());
    }
}
