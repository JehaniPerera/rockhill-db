package com.rockhill.reception_hall.staff;

import java.time.LocalDate;

public class Assignment {
    private int bookingId;
    private LocalDate date;
    private int staffId;
    private AssignmentStatus status;

    public Assignment(int bookingId, LocalDate date, int staffId, AssignmentStatus status) {
        this.bookingId = bookingId;
        this.date = date;
        this.staffId = staffId;
        this.status = status;
    }

    public int getBookingId() { return bookingId; }
    public LocalDate getDate() { return date; }
    public int getStaffId() { return staffId; }
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
}
