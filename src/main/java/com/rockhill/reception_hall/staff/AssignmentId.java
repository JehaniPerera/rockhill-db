package com.rockhill.reception_hall.staff;

import java.io.Serializable;
import java.util.Objects;

public class AssignmentId implements Serializable {
    private int bookingId;
    private int staffId;

    public AssignmentId() { }

    public AssignmentId(int bookingId, int staffId) {
        this.bookingId = bookingId;
        this.staffId = staffId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssignmentId)) return false;
        AssignmentId that = (AssignmentId) o;
        return bookingId == that.bookingId && staffId == that.staffId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, staffId);
    }
}
