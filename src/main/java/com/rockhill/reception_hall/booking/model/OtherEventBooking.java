package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "other_event_booking")
public class OtherEventBooking extends Booking {
    private Long menuId;      // catering menu (Function 2)

    protected OtherEventBooking() {}
    public OtherEventBooking(Customer customer, LocalDate eventDate, EventType type, int guestCount) {
        super(customer, eventDate, type, guestCount);
    }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; touch(); }
}