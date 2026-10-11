package com.rockhill.hallbooking.controller;

import com.rockhill.hallbooking.dto.BookingResponse;
import com.rockhill.hallbooking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only list of bookings, used by the front end booking dropdown. */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRepository bookingRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<BookingResponse> list() {
        return bookingRepository.findAll().stream().map(BookingResponse::from).toList();
    }
}
