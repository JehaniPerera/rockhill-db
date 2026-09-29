package com.rockhill.reception_hall.booking.service;

import com.rockhill.reception_hall.booking.dto.BookingRequest;
import com.rockhill.reception_hall.booking.model.Booking;
import com.rockhill.reception_hall.booking.model.BookingStatus;
import com.rockhill.reception_hall.booking.model.Customer;
import com.rockhill.reception_hall.booking.model.EventType;
import com.rockhill.reception_hall.booking.model.WeddingBooking;
import com.rockhill.reception_hall.booking.model.OtherEventBooking;
import com.rockhill.reception_hall.booking.repository.BookingRepository;
import com.rockhill.reception_hall.booking.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    public BookingService(
            BookingRepository bookingRepository,
            CustomerRepository customerRepository) {

        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
    }

    // Check whether the venue is available on a given date
    public boolean isVenueAvailable(LocalDate date) {

        return !bookingRepository.existsByEventDateAndStatusNot(
                date,
                BookingStatus.Cancelled
        );
    }

    // Create a new booking
    @Transactional
    public Booking createBooking(BookingRequest request) {

        // 1. Check venue availability
        if (!isVenueAvailable(request.getEventDate())) {
            throw new RuntimeException(
                    "Venue is not available on " + request.getEventDate()
            );
        }

        // 2. Find existing customer by email
        Customer customer = customerRepository
                .findByEmail(request.getEmail())
                .orElseGet(() -> {

                    Customer newCustomer = new Customer();

                    newCustomer.setName(request.getCustomerName());
                    newCustomer.setPhone(request.getPhone());
                    newCustomer.setEmail(request.getEmail());

                    return customerRepository.save(newCustomer);
                });

        Booking booking;

        // 3. Create the correct booking type
        if (request.getEventType() == EventType.Wedding) {

            if (request.getPackageId() == null) {
                throw new RuntimeException(
                        "Package ID is required for a wedding booking"
                );
            }

            WeddingBooking weddingBooking = new WeddingBooking();

            weddingBooking.setPackageId(request.getPackageId());

            booking = weddingBooking;

        } else if (request.getEventType() == EventType.Other) {

            if (request.getMenuId() == null) {
                throw new RuntimeException(
                        "Menu ID is required for an other event booking"
                );
            }

            OtherEventBooking otherEventBooking =
                    new OtherEventBooking();

            otherEventBooking.setMenuId(request.getMenuId());

            booking = otherEventBooking;

        } else {
            throw new RuntimeException(
                    "Event type is required"
            );
        }

        // 4. Set common booking details
        booking.setCustomer(customer);
        booking.setEventDate(request.getEventDate());
        booking.setEventType(request.getEventType());
        booking.setGuestCount(request.getGuestCount());
        booking.setStatus(BookingStatus.Pending);
        booking.setTotalPrice(0.0);
        booking.setVenueDamaged(false);

        // 5. Save booking
        return bookingRepository.save(booking);
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