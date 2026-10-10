package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "wedding_booking")
public class WeddingBooking extends Booking {
    public static final int MIN_GUESTS = 100;
    public static final int MAX_GUESTS = 300;

    private Long packageId;   // wedding package (Function 2)

    protected WeddingBooking() {}
    public WeddingBooking(Customer customer, LocalDate eventDate, int guestCount) {
        super(customer, eventDate, EventType.WEDDING, guestCount);
    }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; touch(); }
}