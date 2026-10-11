package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "OtherEventBooking")
@PrimaryKeyJoinColumn(name = "booking_id")
public class OtherEventBooking extends Booking {

    @Column(name = "menu_id", nullable = false)
    private Long menuId;      // catering menu (Function 2)

    /** What kind of event it is, e.g. Birthday, Engagement, Corporate event. */
    @Column(name = "event_description", length = 50)
    private String eventDescription;

    protected OtherEventBooking() {}
    public OtherEventBooking(Customer customer, LocalDate eventDate, EventType type, int guestCount,
                             Long menuId, String eventDescription) {
        super(customer, eventDate, type, guestCount);
        this.menuId = menuId;
        this.eventDescription = eventDescription;
    }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; touch(); }
    public String getEventDescription() { return eventDescription; }
}