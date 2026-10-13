package com.rockhill.hallbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Table HallDamageReport: one row per booking, filled by the staff admin after the event.
 * Only a plain booking_id column is used (no @ManyToOne) so this class does not depend on
 * whichever Booking entity the group finally keeps.
 */
@Entity
@Table(name = "HallDamageReport")
@Getter
@Setter
@NoArgsConstructor
public class HallDamageReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "is_damaged", nullable = false)
    private boolean damaged;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;
}
