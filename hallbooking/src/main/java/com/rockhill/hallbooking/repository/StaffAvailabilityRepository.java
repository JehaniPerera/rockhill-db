package com.rockhill.hallbooking.repository;

import com.rockhill.hallbooking.entity.StaffAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StaffAvailabilityRepository extends JpaRepository<StaffAvailability, Long> {

    boolean existsByStaffStaffIdAndUnavailableDate(Long staffId, LocalDate date);

    Optional<StaffAvailability> findByStaffStaffIdAndUnavailableDate(Long staffId, LocalDate date);

    List<StaffAvailability> findByStaffStaffIdOrderByUnavailableDate(Long staffId);
}
