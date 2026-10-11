package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Booking")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Booking {
    public static final int DEPOSIT_DAYS = 3;
    public static final int BALANCE_DAYS_BEFORE_EVENT = 14;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    /** false = customer is still filling the form (draft); true = customer pressed Submit. */
    @Column(name = "submitted", nullable = false)
    private boolean submitted = false;

    /** Set when the event is marked completed. Payments uses it to decide the Rs. 20,000 deposit refund. */
    @Column(name = "venue_damaged", nullable = false)
    private boolean venueDamaged = false;

    /** Notes written when the venue is reported damaged (used for follow-up billing). */
    @Column(name = "venue_damage_notes", length = 500)
    private String venueDamageNotes;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /** When the Rs. 20,000 deposit was confirmed (null = not received yet). */
    @Column(name = "deposit_received_at")
    private LocalDateTime depositReceivedAt;

    /** How the deposit was confirmed: "AUTO" (found in Payments) or "ADMIN" (confirmed manually). */
    @Column(name = "deposit_confirmed_by", length = 10)
    private String depositConfirmedBy;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt = LocalDateTime.now();

    protected Booking() {}
    protected Booking(Customer customer, LocalDate eventDate, EventType eventType, int guestCount) {
        this.customer = customer; this.eventDate = eventDate;
        this.eventType = eventType; this.guestCount = guestCount;
    }

    /** State pattern guard: all status changes go through here. */
    public void changeStatus(BookingStatus next) {
        if (!status.canMoveTo(next))
            throw new IllegalStateException("Cannot change booking from " + status + " to " + next);
        this.status = next;
        touch();
    }

    /** Keeps the 24-hour draft-expiry clock alive (also called by Function 2 when selections change). */
    public void touch() { this.lastActivityAt = LocalDateTime.now(); }

    /** Deposit is due 3 days after confirmation (calculated, not stored). */
    public LocalDate getDepositDueDate() {
        return confirmedAt == null ? null : confirmedAt.toLocalDate().plusDays(DEPOSIT_DAYS);
    }

    /** Balance is due 14 days before the event (calculated, not stored). */
    public LocalDate getBalanceDueDate() {
        return eventDate == null ? null : eventDate.minusDays(BALANCE_DAYS_BEFORE_EVENT);
    }

    public boolean isDepositReceived() { return depositReceivedAt != null; }
    public String getDepositConfirmedBy() { return depositConfirmedBy; }
    public void markDepositReceived(String by) {
        this.depositReceivedAt = LocalDateTime.now();
        this.depositConfirmedBy = by;
        touch();
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate d) { this.eventDate = d; touch(); }
    public EventType getEventType() { return eventType; }
    public int getGuestCount() { return guestCount; }
    public BookingStatus getStatus() { return status; }
    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean s) { this.submitted = s; touch(); }
    public boolean isVenueDamaged() { return venueDamaged; }
    public void setVenueDamaged(boolean d) { this.venueDamaged = d; }
    public String getVenueDamageNotes() { return venueDamageNotes; }
    public void setVenueDamageNotes(String n) { this.venueDamageNotes = n; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String r) { this.cancellationReason = r; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime t) { this.confirmedAt = t; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
}