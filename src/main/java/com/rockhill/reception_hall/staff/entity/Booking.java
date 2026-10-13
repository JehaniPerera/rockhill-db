package com.rockhill.reception_hall.staff.entity;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "Booking")
public class Booking {

    @Id
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "event_date", nullable = false, insertable = false, updatable = false)
    private LocalDate eventDate;

    @Convert(converter = BookingStatusConverter.class)
    @Column(name = "status", nullable = false, insertable = false, updatable = false)
    private BookingStatus status;

    public Booking() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
