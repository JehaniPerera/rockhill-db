package com.rockhill.reception_hall.staff.controller;

import com.rockhill.reception_hall.staff.dto.BookingResponse;
import com.rockhill.reception_hall.staff.repository.BookingRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only list of bookings, used by the front end booking dropdown. */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<BookingResponse> list() {
        return bookingRepository.findAll().stream().map(BookingResponse::from).toList();
    }

    public BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
}
