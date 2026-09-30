package com.rockhill.reception_hall.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, AssignmentId> {
    List<Assignment> findByDate(LocalDate date);
    List<Assignment> findByBookingId(int bookingId);
}
