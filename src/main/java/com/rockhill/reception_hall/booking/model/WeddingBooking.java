package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "WeddingBooking")
@PrimaryKeyJoinColumn(name = "booking_id")
public class WeddingBooking extends Booking {

    @Column(name = "package_id", nullable = false)
    private Integer packageId;

    public WeddingBooking() {
        super();
    }

    public Integer getPackageId() {
        return packageId;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }
}