package com.rockhill.hallbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Table(name = "Booking")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "event_date", nullable = false, insertable = false, updatable = false)
    private LocalDate eventDate;

    @Convert(converter = BookingStatusConverter.class)
    @Column(name = "status", nullable = false, insertable = false, updatable = false)
    private BookingStatus status;
}
