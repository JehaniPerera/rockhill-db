package com.rockhill.reception_hall.staff.controller;

import com.rockhill.reception_hall.staff.dto.BookingResponse;
import com.rockhill.reception_hall.booking.repository.BookingRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only list of all bookings for the staff page dropdown (admin only, under /api/staff/**). */
@RestController
@RequestMapping("/api/staff/bookings")
public class StaffBookingController {

    private final BookingRepository bookingRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<BookingResponse> list() {
        return bookingRepository.findAll().stream().map(BookingResponse::from).toList();
    }

    public StaffBookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
}
