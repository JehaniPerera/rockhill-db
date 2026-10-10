package com.rockhill.reception_hall.booking.repository;

import com.rockhill.reception_hall.booking.model.Booking;
import com.rockhill.reception_hall.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByEventDateAndStatusIn(LocalDate date, Collection<BookingStatus> statuses);
    boolean existsByEventDateAndStatusInAndIdNot(LocalDate date, Collection<BookingStatus> statuses, Long id);

    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findByStatusAndLastActivityAtBefore(BookingStatus status, LocalDateTime cutoff);
    List<Booking> findByStatusAndDepositDueDateBefore(BookingStatus status, LocalDate date);
}