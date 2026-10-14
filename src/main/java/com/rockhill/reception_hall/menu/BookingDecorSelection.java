package com.rockhill.reception_hall.menu;

import com.rockhill.reception_hall.booking.model.Booking;
import jakarta.persistence.*;

@Entity
@Table(name = "BookingDecorSelection")
@IdClass(BookingDecorSelectionId.class)
public class BookingDecorSelection {

    @Id
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Id
    @ManyToOne
    @JoinColumn(name = "decor_id")
    private DecorOption decorOption;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "charged_amount", nullable = false)
    private Double chargedAmount = 0.0;

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public DecorOption getDecorOption() { return decorOption; }
    public void setDecorOption(DecorOption decorOption) { this.decorOption = decorOption; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getChargedAmount() { return chargedAmount; }
    public void setChargedAmount(Double chargedAmount) { this.chargedAmount = chargedAmount; }
}