package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "OtherEventBooking")
@PrimaryKeyJoinColumn(name = "booking_id")
public class OtherEventBooking extends Booking {

    @Column(name = "menu_id")   // NULL until the customer picks a menu in Function 2
    private Long menuId;

    /** What kind of event it is, e.g. Birthday, Engagement, Corporate event. */
    @Column(name = "event_description", length = 50)
    private String eventDescription;

    protected OtherEventBooking() {}
    public OtherEventBooking(Customer customer, LocalDate eventDate, EventType type, int guestCount,
                             String eventDescription) {
        super(customer, eventDate, type, guestCount);
        this.eventDescription = eventDescription;
    }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; touch(); }
    public String getEventDescription() { return eventDescription; }
}