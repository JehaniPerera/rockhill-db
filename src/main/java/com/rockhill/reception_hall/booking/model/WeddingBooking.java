package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "WeddingBooking")
@PrimaryKeyJoinColumn(name = "booking_id")
public class WeddingBooking extends Booking {
    public static final int MIN_GUESTS = 100;
    public static final int MAX_GUESTS = 300;

    @Column(name = "package_id", nullable = false)
    private Long packageId;   // wedding package (Function 2)

    protected WeddingBooking() {}
    public WeddingBooking(Customer customer, LocalDate eventDate, int guestCount, Long packageId) {
        super(customer, eventDate, EventType.WEDDING, guestCount);
        this.packageId = packageId;
    }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; touch(); }
}