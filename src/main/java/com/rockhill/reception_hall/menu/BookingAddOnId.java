package com.rockhill.reception_hall.menu;

import java.io.Serializable;
import java.util.Objects;

public class BookingAddOnId implements Serializable {

    private Long booking;
    private Integer addOn;

    public BookingAddOnId() {}

    public BookingAddOnId(Long booking, Integer addOn) {
        this.booking = booking;
        this.addOn = addOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingAddOnId)) return false;
        BookingAddOnId that = (BookingAddOnId) o;
        return Objects.equals(booking, that.booking) && Objects.equals(addOn, that.addOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(booking, addOn);
    }
}
