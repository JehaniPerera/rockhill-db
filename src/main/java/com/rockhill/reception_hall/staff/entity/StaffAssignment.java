package com.rockhill.reception_hall.staff.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Table StaffAssignment (booking_id, staff_id, assigned_date), PK = (booking_id, staff_id). */
@Entity
@Table(name = "StaffAssignment")
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

    public StaffAssignment() {
    }

    public StaffAssignmentId getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public Staff getStaff() {
        return staff;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setId(StaffAssignmentId id) {
        this.id = id;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }
}
