package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "WeddingBooking")
@PrimaryKeyJoinColumn(name = "booking_id")
public class WeddingBooking extends Booking {
    public static final int MIN_GUESTS = 100;
    public static final int MAX_GUESTS = 300;

    @Column(name = "package_id")   // NULL until the customer picks a package in Function 2
    private Long packageId;

    protected WeddingBooking() {}
    public WeddingBooking(Customer customer, LocalDate eventDate, int guestCount) {
        super(customer, eventDate, EventType.WEDDING, guestCount);
    }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; touch(); }
}