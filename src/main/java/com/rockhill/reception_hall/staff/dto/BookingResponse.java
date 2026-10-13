package com.rockhill.reception_hall.staff.dto;

import com.rockhill.reception_hall.staff.entity.Booking;

import java.time.LocalDate;

public record BookingResponse(Long bookingId, LocalDate eventDate, String status) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getBookingId(), b.getEventDate(), b.getStatus().getDbValue());
    }
}
