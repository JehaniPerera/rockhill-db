package com.rockhill.reception_hall.menu;

import com.rockhill.reception_hall.booking.model.Booking;
import jakarta.persistence.*;

@Entity
@Table(name = "BookingAddOn")
@IdClass(BookingAddOnId.class)
public class BookingAddOn {

    @Id
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Id
    @ManyToOne
    @JoinColumn(name = "addon_id")
    private AddOn addOn;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "charged_amount", nullable = false)
    private Double chargedAmount = 0.0;

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public AddOn getAddOn() { return addOn; }
    public void setAddOn(AddOn addOn) { this.addOn = addOn; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getChargedAmount() { return chargedAmount; }
    public void setChargedAmount(Double chargedAmount) { this.chargedAmount = chargedAmount; }
}