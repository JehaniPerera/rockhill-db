package com.rockhill.reception_hall.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingDecorSelectionRepository extends JpaRepository<BookingDecorSelection, BookingDecorSelectionId> {
    List<BookingDecorSelection> findByBooking_Id(Long bookingId);
}