package com.rockhill.reception_hall.booking.service;

import com.rockhill.reception_hall.booking.model.Booking;
import com.rockhill.reception_hall.booking.model.BookingStatus;
import com.rockhill.reception_hall.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Check whether the venue is available on a given date
    public boolean isVenueAvailable(LocalDate date) {

        return !bookingRepository.existsByEventDateAndStatusNot(
                date,
                BookingStatus.Cancelled
        );
    }

    // Get all bookings
    public List<Booking> getAllBookings() {

        return bookingRepository.findAll();
    }


    // Get one booking by ID
    public Booking getBookingById(Integer bookingId) {

        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found")
                );
    }

    // Confirm a booking
    public Booking confirmBooking(Integer bookingId) {

        Booking booking = getBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.Cancelled) {
            throw new RuntimeException(
                    "Cancelled booking cannot be confirmed"
            );
        }

        booking.setStatus(BookingStatus.Confirmed);

        return bookingRepository.save(booking);
    }

    // Cancel a booking
    public Booking cancelBooking(Integer bookingId) {

        Booking booking = getBookingById(bookingId);

        booking.setStatus(BookingStatus.Cancelled);

        return bookingRepository.save(booking);
    }

    // Reschedule a booking
    public Booking rescheduleBooking(
            Integer bookingId,
            LocalDate newDate) {

        Booking booking = getBookingById(bookingId);

        if (!isVenueAvailable(newDate)) {
            throw new RuntimeException(
                    "Venue is not available on the new date"
            );
        }

        booking.setEventDate(newDate);

        return bookingRepository.save(booking);
    }
}