package com.rockhill.reception_hall.booking.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "booking")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private int guestCount;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    /** false = customer is still filling the form (draft); true = customer pressed Submit. */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean submitted = false;

    private LocalDate depositDueDate;   // confirmation date + 7 days
    private LocalDate balanceDueDate;   // event date - 14 days
    private String cancellationReason;
    private LocalDateTime confirmedAt;
    private LocalDateTime lastActivityAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "booking_addon_ids", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "addon_id")
    private Set<Long> addOnIds = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "booking_decor_ids", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "decor_id")
    private Set<Long> decorIds = new LinkedHashSet<>();

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

    public void touch() { this.lastActivityAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate d) { this.eventDate = d; touch(); }
    public EventType getEventType() { return eventType; }
    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int g) { this.guestCount = g; touch(); }
    public BookingStatus getStatus() { return status; }
    public boolean isSubmitted() { return submitted; }
    public void setSubmitted(boolean s) { this.submitted = s; touch(); }
    public LocalDate getDepositDueDate() { return depositDueDate; }
    public void setDepositDueDate(LocalDate d) { this.depositDueDate = d; }
    public LocalDate getBalanceDueDate() { return balanceDueDate; }
    public void setBalanceDueDate(LocalDate d) { this.balanceDueDate = d; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String r) { this.cancellationReason = r; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime t) { this.confirmedAt = t; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<Long> getAddOnIds() { return addOnIds; }
    public Set<Long> getDecorIds() { return decorIds; }
}