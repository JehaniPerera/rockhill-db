package com.rockhill.hallbooking.repository;

import com.rockhill.hallbooking.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    /**
     * Staff/vendors free on the given date: not marked unavailable and not already
     * assigned to another event. Pass role = "" for no role filter.
     */
    @Query("""
            select s from Staff s
            where (:role = '' or lower(s.role) = lower(:role))
              and not exists (select 1 from StaffAvailability a
                              where a.staff = s and a.unavailableDate = :date)
              and not exists (select 1 from StaffAssignment sa
                              where sa.staff = s and sa.assignedDate = :date)
            order by s.name
            """)
    List<Staff> findAvailable(@Param("date") LocalDate date, @Param("role") String role);
}
