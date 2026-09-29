package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "OtherEventBooking")
@PrimaryKeyJoinColumn(name = "booking_id")
public class OtherEventBooking extends Booking {

    @Column(name = "menu_id", nullable = false)
    private Integer menuId;

    public OtherEventBooking() {
        super();
    }

    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }
}