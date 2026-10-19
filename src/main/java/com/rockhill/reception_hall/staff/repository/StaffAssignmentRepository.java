package com.rockhill.reception_hall.staff.repository;

import com.rockhill.reception_hall.booking.model.BookingStatus;
import com.rockhill.reception_hall.staff.entity.StaffAssignment;
import com.rockhill.reception_hall.staff.entity.StaffAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, StaffAssignmentId> {

    @Query("select a from StaffAssignment a where a.booking.id = :bookingId")
    List<StaffAssignment> findByBookingId(@Param("bookingId") Long bookingId);

    boolean existsByStaffStaffId(Long staffId);

    boolean existsByStaffStaffIdAndAssignedDate(Long staffId, LocalDate date);

    boolean existsByStaffStaffIdAndAssignedDateAndBookingStatus(Long staffId, LocalDate date, BookingStatus status);

    boolean existsByStaffStaffIdAndBookingStatusIn(Long staffId, Collection<BookingStatus> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from StaffAssignment a where a.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
