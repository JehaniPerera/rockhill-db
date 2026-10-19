package com.rockhill.reception_hall.staff.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

/** Composite primary key (booking_id, staff_id) of StaffAssignment. */
@Embeddable
public class StaffAssignmentId implements Serializable {

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "staff_id")
    private Long staffId;

    public StaffAssignmentId() {
    }

    public StaffAssignmentId(Long bookingId, Long staffId) {
        this.bookingId = bookingId;
        this.staffId = staffId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffAssignmentId that = (StaffAssignmentId) o;
        return java.util.Objects.equals(bookingId, that.bookingId) &&
                java.util.Objects.equals(staffId, that.staffId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(bookingId, staffId);
    }
}
