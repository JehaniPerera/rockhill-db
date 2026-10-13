package com.rockhill.reception_hall.booking.service;

import com.rockhill.reception_hall.booking.model.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/** Factory pattern: picks the right Booking subclass from the event type. */
@Component
public class BookingFactory {
    public Booking create(Customer c, LocalDate date, EventType type, int guests, String eventDescription) {
        return type == EventType.WEDDING
                ? new WeddingBooking(c, date, guests)
                : new OtherEventBooking(c, date, type, guests, eventDescription);
    }
}