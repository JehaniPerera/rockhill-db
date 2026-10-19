package com.rockhill.reception_hall.staff.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Table HallDamageReport: one row per booking, filled by the staff admin after the event.
 * Only a plain booking_id column is used (no @ManyToOne) so this class does not depend on
 * whichever Booking entity the group finally keeps.
 */
@Entity
@Table(name = "HallDamageReport")
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

    public HallDamageReport() {
    }

    public Long getReportId() {
        return reportId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }
}
