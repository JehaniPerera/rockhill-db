package com.rockhill.reception_hall.staff.repository;

import com.rockhill.reception_hall.staff.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
