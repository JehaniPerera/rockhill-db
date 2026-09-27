package com.rockhill.reception_hall.booking.controller;

import com.rockhill.reception_hall.booking.model.Booking;
import com.rockhill.reception_hall.booking.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/bookings")
@CrossOrigin
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // 1. Check venue availability
    @GetMapping("/availability")
    public Map<String, Object> checkAvailability(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        boolean available =
                bookingService.isVenueAvailable(date);

        return Map.of(
                "date", date,
                "available", available
        );
    }

    // 2. Get all bookings
    @GetMapping
    public List<Booking> getAllBookings() {

        return bookingService.getAllBookings();
    }

    // 3. Get booking by ID
    @GetMapping("/{id}")
    public Booking getBookingById(
            @PathVariable Integer id) {

        return bookingService.getBookingById(id);
    }

    // 4. Confirm booking
    @PutMapping("/{id}/confirm")
    public Booking confirmBooking(
            @PathVariable Integer id) {

        return bookingService.confirmBooking(id);
    }

    // 5. Cancel booking
    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(
            @PathVariable Integer id) {

        return bookingService.cancelBooking(id);
    }

    // 6. Reschedule booking
    @PutMapping("/{id}/reschedule")
    public Booking rescheduleBooking(
            @PathVariable Integer id,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate newDate) {

        return bookingService.rescheduleBooking(
                id,
                newDate
        );
    }
}
