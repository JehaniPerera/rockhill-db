package com.rockhill.hallbooking.repository;

import com.rockhill.hallbooking.entity.HallDamageReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HallDamageReportRepository extends JpaRepository<HallDamageReport, Long> {

    Optional<HallDamageReport> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);
}
