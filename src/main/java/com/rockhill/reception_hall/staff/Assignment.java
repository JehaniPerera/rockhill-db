package com.rockhill.reception_hall.staff;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "StaffAssignment")
@IdClass(AssignmentId.class)
public class Assignment {

    @Id
    @Column(name = "booking_id")
    private int bookingId;

    @Id
    @Column(name = "staff_id")
    private int staffId;

    @Column(name = "assigned_date")
    private LocalDate date;

    protected Assignment() { }

    public Assignment(int bookingId, int staffId, LocalDate date) {
        this.bookingId = bookingId;
        this.staffId = staffId;
        this.date = date;
    }

    public int getBookingId() { return bookingId; }
    public int getStaffId() { return staffId; }
    public LocalDate getDate() { return date; }
}
