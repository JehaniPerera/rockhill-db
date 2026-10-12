package com.rockhill.reception_hall.booking.service;

import com.rockhill.reception_hall.booking.dto.BookingRequest;
import com.rockhill.reception_hall.booking.event.BookingEvent;
import com.rockhill.reception_hall.booking.event.BookingEvent.Type;
import com.rockhill.reception_hall.booking.model.*;
import com.rockhill.reception_hall.booking.repository.BookingRepository;
import com.rockhill.reception_hall.booking.repository.CustomerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
@Transactional
public class BookingService {
    // Same rule as the database: every status except Cancelled holds the date.
    private static final EnumSet<BookingStatus> DATE_BLOCKING =
            EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.COMPLETED);
    public static final int DRAFT_EXPIRY_HOURS = 24;
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookings;
    private final CustomerRepository customers;
    private final BookingFactory factory;
    private final ApplicationEventPublisher publisher;
    private final JdbcTemplate jdbc;

    public BookingService(BookingRepository bookings, CustomerRepository customers, BookingFactory factory,
                          ApplicationEventPublisher publisher, JdbcTemplate jdbc) {
        this.bookings = bookings; this.customers = customers;
        this.factory = factory; this.publisher = publisher; this.jdbc = jdbc;
    }

    // ---- Scenario 1: Check venue availability ----
    @Transactional(readOnly = true)
    public boolean isDateAvailable(LocalDate date) {
        return !bookings.existsByEventDateAndStatusIn(date, DATE_BLOCKING);
    }

    /** Scenario 1, alternate 2a: the date must be a valid future date. */
    public void requireFutureDate(LocalDate date) {
        if (date == null || !date.isAfter(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose a valid future date");
    }

    /** Scenario 1, alternate 3a: the nearest open dates around a date that is taken. */
    @Transactional(readOnly = true)
    public List<LocalDate> nearestAvailableDates(LocalDate around, int count) {
        List<LocalDate> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int offset = 1; offset <= 120 && result.size() < count; offset++) {
            LocalDate after = around.plusDays(offset);
            if (isDateAvailable(after)) result.add(after);
            if (result.size() >= count) break;
            LocalDate before = around.minusDays(offset);
            if (before.isAfter(today) && isDateAvailable(before)) result.add(before);
        }
        return result;
    }

    // ---- Scenarios 2 & 3: create the Pending booking as a draft (Option A) ----
    public Booking createPending(BookingRequest r) {
        validateGuestCount(r.eventType(), r.guestCount());
        if (!isDateAvailable(r.eventDate()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Date is not available");
        Customer c = findOrCreateCustomer(r);
        Booking b = factory.create(c, r.eventDate(), r.eventType(), r.guestCount(),
                r.selectionId(), r.eventDescription());
        try {
            return bookings.saveAndFlush(b);   // DB unique constraint on active_event_date is the final guard
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Could not save the booking: the date was just taken, or the selected package/menu does not exist");
        }
    }

    private Customer findOrCreateCustomer(BookingRequest r) {
        String phone = r.customerPhone().trim();
        String email = (r.customerEmail() == null || r.customerEmail().isBlank()) ? null : r.customerEmail().trim();
        return customers.findByPhone(phone).stream().findFirst().orElseGet(() -> {
            if (email != null && customers.findFirstByEmail(email).isPresent())
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "This email is already registered with a different phone number");
            return customers.save(new Customer(r.customerName().trim(), phone, email));
        });
    }

    private void validateGuestCount(EventType type, int guests) {
        if (type == EventType.WEDDING &&
                (guests < WeddingBooking.MIN_GUESTS || guests > WeddingBooking.MAX_GUESTS))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Wedding guest count must be between 100 and 300");
    }

    // ---- package / menu (add-ons and decor belong to Function 2's BookingSelectionService) ----
    public Booking attachPackage(Long id, Long packageId) {
        Booking b = editableBooking(id);
        if (!(b instanceof WeddingBooking w))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Packages are for wedding bookings only");
        w.setPackageId(packageId);
        b.touch();
        flushOrBadRequest("The selected package does not exist");
        return b;
    }

    public Booking attachMenu(Long id, Long menuId) {
        Booking b = editableBooking(id);
        if (!(b instanceof OtherEventBooking o))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menus are for non-wedding events only");
        o.setMenuId(menuId);
        b.touch();
        flushOrBadRequest("The selected menu does not exist");
        return b;
    }

    private void flushOrBadRequest(String message) {
        try { bookings.flush(); }
        catch (DataIntegrityViolationException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
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
        b.setSubmitted(true);
        publisher.publishEvent(new BookingEvent(b, Type.SUBMITTED));   // notifies the admin
        return b;
    }

    // ---- Scenario 4: Admin confirm or reject ----
    /**
     * Admin ACCEPTS the request: availability is approved but the booking stays PENDING.
     * It becomes CONFIRMED only when the Rs. 20,000 deposit is received (within 3 days).
     */
    public Booking confirm(Long id) {
        Booking b = get(id);
        requireSubmitted(b);
        if (b.getStatus() != BookingStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a Pending booking can be accepted");
        if (b.getConfirmedAt() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already accepted - waiting for the deposit");
        b.setConfirmedAt(LocalDateTime.now());   // approval time: deposit due date = this + 3 days
        b.touch();
        if (b.isDepositReceived())               // re-approval after a customer reschedule: deposit already paid
            b.changeStatus(BookingStatus.CONFIRMED);
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

    // ---- Scenario 4: deposit (Rs. 20,000) must be received within 3 days of confirmation ----
    /** Manual: the administrator confirms that the deposit was received. */
    public Booking confirmDepositManually(Long id) {
        return receiveDeposit(id, "ADMIN");
    }

    /** Automatic: also callable by the Payments code the moment it records a Deposit payment. */
    public Booking confirmDepositAutomatically(Long id) {
        return receiveDeposit(id, "AUTO");
    }

    private Booking receiveDeposit(Long id, String by) {
        Booking b = get(id);
        if (b.getStatus() != BookingStatus.PENDING || b.getConfirmedAt() == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The deposit can only be received for a Pending booking that the admin has accepted");
        if (b.isDepositReceived())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The deposit was already confirmed");
        b.markDepositReceived(by);
        b.changeStatus(BookingStatus.CONFIRMED);   // deposit received = booking Confirmed
        publisher.publishEvent(new BookingEvent(b, Type.DEPOSIT_RECEIVED));
        return b;
    }

    /** Automatic check: confirms every accepted Pending booking that already has a Deposit row in Payments. */
    public int autoConfirmDeposits() {
        int count = 0;
        for (Booking b : bookings.findByStatusAndSubmittedTrue(BookingStatus.PENDING)) {
            if (b.getConfirmedAt() != null && !b.isDepositReceived() && Boolean.TRUE.equals(depositPaymentExists(b))) {
                b.markDepositReceived("AUTO");
                b.changeStatus(BookingStatus.CONFIRMED);
                publisher.publishEvent(new BookingEvent(b, Type.DEPOSIT_RECEIVED));
                count++;
            }
        }
        return count;
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
        if (!b.getEventDate().isAfter(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The event date has passed, so this booking cannot be rescheduled");
        if (!newDate.isAfter(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New date must be in the future");
        if (bookings.existsByEventDateAndStatusInAndIdNot(newDate, DATE_BLOCKING, id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "New date is not available");
        boolean wasConfirmed = b.getStatus() == BookingStatus.CONFIRMED;
        b.setEventDate(newDate);   // the balance due date is calculated from the event date
        syncStaffDate(b);          // StaffAssignment.assigned_date must always equal the event date
        if (byCustomer && (wasConfirmed || b.getConfirmedAt() != null)) {
            // A customer date change needs the admin to accept the new date again.
            if (wasConfirmed) b.changeStatus(BookingStatus.PENDING);   // deposit stays recorded as received
            b.setConfirmedAt(null);                                    // back to "waiting for admin"
            publisher.publishEvent(new BookingEvent(b, Type.RESCHEDULE_REQUESTED));
            return b;
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
        releaseStaff(b);   // step 5: free assigned staff/vendors
        publisher.publishEvent(new BookingEvent(b, Type.CANCELLED));
        return b;
    }

    // ---- Scenario 7: Mark completed (records the venue damage status) ----
    public Booking markCompleted(Long id, boolean venueDamaged, String damageNotes) {
        Booking b = get(id);
        guard(b, BookingStatus.COMPLETED);
        if (b.getEventDate().isAfter(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Event has not taken place yet");
        b.setVenueDamaged(venueDamaged);   // Payments uses this flag for the Rs. 20,000 deposit refund
        b.setVenueDamageNotes(venueDamaged && damageNotes != null && !damageNotes.isBlank()
                ? damageNotes.trim() : null);   // notes are kept for follow-up billing
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
        return customers.findByPhone(phone).stream()
                .flatMap(c -> bookings.findByCustomerId(c.getId()).stream()).toList();
    }

    private void guard(Booking b, BookingStatus target) {
        if (!b.getStatus().canMoveTo(target))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change booking from " + b.getStatus() + " to " + target);
    }

    /** Keeps Function 3's StaffAssignment.assigned_date equal to the booking's (new) event date. */
    private void syncStaffDate(Booking b) {
        try {
            jdbc.update("UPDATE StaffAssignment SET assigned_date = ? WHERE booking_id = ?", b.getEventDate(), b.getId());
        } catch (DataAccessException ex) {
            log.warn("Could not update staff assignment date for booking #{}: {}", b.getId(), ex.getMessage());
        }
    }

    /** Removes the booking's rows in Function 3's StaffAssignment table. */
    private void releaseStaff(Booking b) {
        try {
            jdbc.update("DELETE FROM StaffAssignment WHERE booking_id = ?", b.getId());
        } catch (DataAccessException ex) {
            // Function 3's table is missing or named differently: do not block the cancellation
            log.warn("Could not release staff for booking #{}: {}", b.getId(), ex.getMessage());
        }
    }

    // ---- Scheduled clean-up: releases dates held by abandoned drafts and unpaid bookings ----
    @Scheduled(fixedRate = 900_000)   // every 15 minutes
    public void expireStaleBookings() {
        LocalDateTime draftCutoff = LocalDateTime.now().minusHours(DRAFT_EXPIRY_HOURS);
        for (Booking b : bookings.findByStatusAndSubmittedFalseAndLastActivityAtBefore(BookingStatus.PENDING, draftCutoff)) {
            b.changeStatus(BookingStatus.CANCELLED);
            b.setCancellationReason("Expired: draft not submitted within " + DRAFT_EXPIRY_HOURS + " hours");
            publisher.publishEvent(new BookingEvent(b, Type.CANCELLED));
        }
        autoConfirmDeposits();   // automatic deposit confirmation from the Payment table
        // Scenario 4, alternate 6a: deposit not received within 3 days of the admin accepting.
        // The booking is still PENDING (accepted, awaiting deposit); drafts have no approval time.
        LocalDateTime depositCutoff = LocalDateTime.now().minusDays(Booking.DEPOSIT_DAYS);
        for (Booking b : bookings.findByStatusAndConfirmedAtBefore(BookingStatus.PENDING, depositCutoff)) {
            if (b.isDepositReceived()) continue;
            Boolean paid = depositPaymentExists(b);
            if (paid == null || paid) continue;   // cannot check the Payment table, or it is paid
            b.changeStatus(BookingStatus.CANCELLED);
            b.setCancellationReason("Deposit not received within " + Booking.DEPOSIT_DAYS + " days");
            releaseStaff(b);
            publisher.publishEvent(new BookingEvent(b, Type.DEPOSIT_OVERDUE));
        }
    }

    /** Reads the Payment table (Function 4): TRUE/FALSE = Deposit row exists or not, null = cannot check. */
    protected Boolean depositPaymentExists(Booking b) {
        try {
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM Payment WHERE booking_id = ? AND payment_type = 'Deposit'",
                    Integer.class, b.getId());
            return n != null && n > 0;
        } catch (DataAccessException ex) {
            log.warn("Could not read Payment for booking #{}: {}", b.getId(), ex.getMessage());
            return null;
        }
    }
}
