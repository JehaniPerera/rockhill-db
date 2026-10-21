package com.rockhill.reception_hall.staff.dto;

import com.rockhill.reception_hall.booking.model.Booking;

import java.time.LocalDate;

public record BookingResponse(Long bookingId, LocalDate eventDate, String status) {

    public static BookingResponse from(Booking b) {
        String s = b.getStatus().name();   // PENDING -> "Pending" (the staff page colours its tags by this)
        return new BookingResponse(b.getId(), b.getEventDate(), s.charAt(0) + s.substring(1).toLowerCase());
    }
}
