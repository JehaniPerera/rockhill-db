package com.rockhill.reception_hall.booking.repository;

import com.rockhill.reception_hall.booking.model.Booking;
import com.rockhill.reception_hall.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByEventDate(LocalDate eventDate);

    boolean existsByEventDateAndStatusNot(
            LocalDate eventDate,
            BookingStatus status
    );

    List<Booking> findByStatusIn(
            List<BookingStatus> statuses
    );
}