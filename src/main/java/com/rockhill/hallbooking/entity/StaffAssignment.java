package com.rockhill.hallbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Table StaffAssignment (booking_id, staff_id, assigned_date), PK = (booking_id, staff_id). */
@Entity
@Table(name = "StaffAssignment")
@Getter
@Setter
@NoArgsConstructor
public class StaffAssignment {

    @EmbeddedId
    private StaffAssignmentId id;

    @MapsId("bookingId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @MapsId("staffId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;
}
