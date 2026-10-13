package com.rockhill.reception_hall.staff.repository;

import com.rockhill.reception_hall.staff.entity.BookingStatus;
import com.rockhill.reception_hall.staff.entity.StaffAssignment;
import com.rockhill.reception_hall.staff.entity.StaffAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, StaffAssignmentId> {

    List<StaffAssignment> findByBookingBookingId(Long bookingId);

    boolean existsByStaffStaffId(Long staffId);

    boolean existsByStaffStaffIdAndAssignedDate(Long staffId, LocalDate date);

    boolean existsByStaffStaffIdAndAssignedDateAndBookingStatus(Long staffId, LocalDate date, BookingStatus status);

    boolean existsByStaffStaffIdAndBookingStatusIn(Long staffId, Collection<BookingStatus> statuses);

    void deleteByBookingBookingId(Long bookingId);
}
