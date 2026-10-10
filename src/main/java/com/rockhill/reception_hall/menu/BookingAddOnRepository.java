package com.rockhill.reception_hall.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingAddOnRepository extends JpaRepository<BookingAddOn, BookingAddOnId> {
    List<BookingAddOn> findByBooking_Id(Long bookingId);
}
