package com.rockhill.reception_hall.booking.service;

import com.rockhill.reception_hall.booking.dto.BookingRequest;
import com.rockhill.reception_hall.booking.event.BookingEvent;
import com.rockhill.reception_hall.booking.event.BookingEvent.Type;
import com.rockhill.reception_hall.booking.model.*;
import com.rockhill.reception_hall.booking.repository.BookingRepository;
import com.rockhill.reception_hall.booking.repository.CustomerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
@Transactional
public class BookingService {
    private static final EnumSet<BookingStatus> DATE_BLOCKING = EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
    public static final int DEPOSIT_DAYS = 7;
    public static final int BALANCE_DAYS_BEFORE_EVENT = 14;
    public static final int DRAFT_EXPIRY_HOURS = 24;

    private final BookingRepository bookings;
    private final CustomerRepository customers;
    private final BookingFactory factory;
    private final ApplicationEventPublisher publisher;

    public BookingService(BookingRepository bookings, CustomerRepository customers,
                          BookingFactory factory, ApplicationEventPublisher publisher) {
        this.bookings = bookings; this.customers = customers;
        this.factory = factory; this.publisher = publisher;
    }

    // ---- Scenario 1: Check venue availability ----
    @Transactional(readOnly = true)
    public boolean isDateAvailable(LocalDate date) {
        return !bookings.existsByEventDateAndStatusIn(date, DATE_BLOCKING);
    }

    // ---- Scenarios 2 & 3: create the Pending booking as a draft (Option A) ----
    public Booking createPending(BookingRequest r) {
        validateGuestCount(r.eventType(), r.guestCount());
        if (!isDateAvailable(r.eventDate()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Date is not available");
        Customer c = customers.findByPhone(r.customerPhone())
                .orElseGet(() -> customers.save(new Customer(r.customerName(), r.customerPhone(), r.customerEmail())));
        Booking b = factory.create(c, r.eventDate(), r.eventType(), r.guestCount());
        try {
            return bookings.saveAndFlush(b);   // DB unique constraint is the final guard against double-booking
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Date is not available");
        }
    }

    private void validateGuestCount(EventType type, int guests) {
        if (type == EventType.WEDDING &&
                (guests < WeddingBooking.MIN_GUESTS || guests > WeddingBooking.MAX_GUESTS))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Wedding guest count must be between 100 and 300");
    }

    // ---- selections attach to the Pending booking ----
    public Booking attachPackage(Long id, Long packageId) {
        Booking b = editableBooking(id);
        if (!(b instanceof WeddingBooking w))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Packages are for wedding bookings only");
        w.setPackageId(packageId);
        return b;
    }

    public Booking attachMenu(Long id, Long menuId) {
        Booking b = editableBooking(id);
        if (!(b instanceof OtherEventBooking o))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menus are for non-wedding events only");
        o.setMenuId(menuId);
        return b;
    }

    public Booking setAddOns(Long id, List<Long> addOnIds) {
        Booking b = editableBooking(id);
        b.getAddOnIds().clear(); b.getAddOnIds().addAll(addOnIds); b.touch();
        return b;
    }

    public Booking setDecor(Long id, List<Long> decorIds) {
        Booking b = editableBooking(id);
        b.getDecorIds().clear(); b.getDecorIds().addAll(decorIds); b.touch();
        return b;
    }

    private Booking editableBooking(Long id) {
        Booking b = get(id);
        if (b.getStatus() != BookingStatus.PENDING || b.isSubmitted())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking can no longer be edited");
        return b;
    }

    // ---- customer presses the final Submit ----
    public Booking submit(Long id) {
        Booking b = editableBooking(id);
        if (b instanceof WeddingBooking w && w.getPackageId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a wedding package first");
        if (b instanceof OtherEventBooking o && o.getMenuId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a catering menu first");
        b.setSubmitted(true);
        publisher.publishEvent(new BookingEvent(b, Type.SUBMITTED));   // notifies the admin
        return b;
    }

    // ---- Scenario 4: Admin confirm or reject ----
    public Booking confirm(Long id) {
        Booking b = get(id);
        requireSubmitted(b);
        guard(b, BookingStatus.CONFIRMED);
        b.changeStatus(BookingStatus.CONFIRMED);
        b.setConfirmedAt(LocalDateTime.now());
        b.setDepositDueDate(LocalDate.now().plusDays(DEPOSIT_DAYS));
        b.setBalanceDueDate(b.getEventDate().minusDays(BALANCE_DAYS_BEFORE_EVENT));
        publisher.publishEvent(new BookingEvent(b, Type.CONFIRMED));
        return b;
    }

    public Booking reject(Long id, String reason) {
        if (reason == null || reason.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
        Booking b = get(id);
        requireSubmitted(b);
        guard(b, BookingStatus.CANCELLED);
        b.changeStatus(BookingStatus.CANCELLED);
        b.setCancellationReason(reason);
        publisher.publishEvent(new BookingEvent(b, Type.REJECTED));
        return b;
    }

    private void requireSubmitted(Booking b) {
        if (!b.isSubmitted())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The customer has not submitted this booking yet");
    }

    // ---- Scenario 5: Reschedule ----
    public Booking reschedule(Long id, LocalDate newDate, boolean byCustomer) {
        Booking b = get(id);
        if (b.getStatus() != BookingStatus.PENDING && b.getStatus() != BookingStatus.CONFIRMED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only Pending or Confirmed bookings can be rescheduled");
        if (!newDate.isAfter(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New date must be in the future");
        if (bookings.existsByEventDateAndStatusInAndIdNot(newDate, DATE_BLOCKING, id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "New date is not available");
        boolean wasConfirmed = b.getStatus() == BookingStatus.CONFIRMED;
        b.setEventDate(newDate);
        if (wasConfirmed) {
            b.setBalanceDueDate(newDate.minusDays(BALANCE_DAYS_BEFORE_EVENT));
            if (byCustomer) {   // customer change on a Confirmed booking needs admin approval again
                b.changeStatus(BookingStatus.PENDING);
                publisher.publishEvent(new BookingEvent(b, Type.RESCHEDULE_REQUESTED));
                return b;
            }
        }
        publisher.publishEvent(new BookingEvent(b, Type.RESCHEDULED));
        return b;
    }

    // ---- Scenario 6: Cancel ----
    public Booking cancel(Long id, String reason) {
        Booking b = get(id);
        guard(b, BookingStatus.CANCELLED);
        b.changeStatus(BookingStatus.CANCELLED);
        b.setCancellationReason(reason == null || reason.isBlank() ? "Cancelled by request" : reason);
        publisher.publishEvent(new BookingEvent(b, Type.CANCELLED));
        return b;
    }

    // ---- Scenario 7: Mark completed ----
    public Booking markCompleted(Long id) {
        Booking b = get(id);
        guard(b, BookingStatus.COMPLETED);
        if (b.getEventDate().isAfter(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Event has not taken place yet");
        b.changeStatus(BookingStatus.COMPLETED);
        publisher.publishEvent(new BookingEvent(b, Type.COMPLETED));
        return b;
    }

    // ---- queries ----
    @Transactional(readOnly = true)
    public Booking get(Long id) {
        return bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    /** Admin list: Pending shows only submitted bookings (drafts are hidden). */
    @Transactional(readOnly = true)
    public List<Booking> byStatus(BookingStatus s) {
        return s == BookingStatus.PENDING ? bookings.findByStatusAndSubmittedTrue(s) : bookings.findByStatus(s);
    }

    @Transactional(readOnly = true)
    public List<Booking> byCustomer(Long customerId) { return bookings.findByCustomerId(customerId); }

    @Transactional(readOnly = true)
    public List<Booking> byPhone(String phone) {
        return customers.findByPhone(phone).map(c -> bookings.findByCustomerId(c.getId())).orElse(List.of());
    }

    private void guard(Booking b, BookingStatus target) {
        if (!b.getStatus().canMoveTo(target))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change booking from " + b.getStatus() + " to " + target);
    }

    // ---- Scheduled clean-up: releases dates held by abandoned drafts and unpaid bookings ----
    @Scheduled(fixedRate = 3_600_000)
    public void expireStaleBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(DRAFT_EXPIRY_HOURS);
        for (Booking b : bookings.findByStatusAndSubmittedFalseAndLastActivityAtBefore(BookingStatus.PENDING, cutoff)) {
            b.changeStatus(BookingStatus.CANCELLED);
            b.setCancellationReason("Expired: draft not submitted within " + DRAFT_EXPIRY_HOURS + " hours");
            publisher.publishEvent(new BookingEvent(b, Type.CANCELLED));
        }
        // Deposit not received within 7 days of confirmation (scenario 4, alternate 6a).
        for (Booking b : bookings.findByStatusAndDepositDueDateBefore(BookingStatus.CONFIRMED, LocalDate.now())) {
            if (!isDepositPaid(b)) {
                b.changeStatus(BookingStatus.CANCELLED);
                b.setCancellationReason("Deposit not received within " + DEPOSIT_DAYS + " days");
                publisher.publishEvent(new BookingEvent(b, Type.DEPOSIT_OVERDUE));
            }
        }
    }

    /** TODO: replace with the payment team's lookup. Always false for now, so test this job carefully. */
    protected boolean isDepositPaid(Booking b) { return false; }
}