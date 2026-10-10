package com.rockhill.hallbooking.repository;

import com.rockhill.hallbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
