package com.rockhill.reception_hall.menu;

import java.io.Serializable;
import java.util.Objects;

public class BookingDecorSelectionId implements Serializable {

    private Long booking;
    private Integer decorOption;

    public BookingDecorSelectionId() {}

    public BookingDecorSelectionId(Long booking, Integer decorOption) {
        this.booking = booking;
        this.decorOption = decorOption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingDecorSelectionId)) return false;
        BookingDecorSelectionId that = (BookingDecorSelectionId) o;
        return Objects.equals(booking, that.booking) && Objects.equals(decorOption, that.decorOption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(booking, decorOption);
    }
}