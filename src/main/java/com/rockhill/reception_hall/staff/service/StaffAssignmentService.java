package com.rockhill.reception_hall.staff.service;

import com.rockhill.reception_hall.staff.dto.AssignRequest;
import com.rockhill.reception_hall.staff.dto.AssignmentResponse;
import com.rockhill.reception_hall.staff.dto.ReassignRequest;
import com.rockhill.reception_hall.staff.dto.StaffResponse;
import com.rockhill.reception_hall.staff.entity.Booking;
import com.rockhill.reception_hall.staff.entity.BookingStatus;
import com.rockhill.reception_hall.staff.entity.Staff;
import com.rockhill.reception_hall.staff.entity.StaffAssignment;
import com.rockhill.reception_hall.staff.entity.StaffAssignmentId;
import com.rockhill.reception_hall.staff.exception.BadRequestException;
import com.rockhill.reception_hall.staff.exception.ConflictException;
import com.rockhill.reception_hall.staff.exception.ResourceNotFoundException;
import com.rockhill.reception_hall.staff.repository.BookingRepository;
import com.rockhill.reception_hall.staff.repository.StaffAssignmentRepository;
import com.rockhill.reception_hall.staff.repository.StaffAvailabilityRepository;
import com.rockhill.reception_hall.staff.repository.StaffRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Use case: Assign Staff/Vendor to Booking.
 * "Marks the assigned staff/vendor as unavailable" (step 5) is achieved by the StaffAssignment
 * row itself: a staff/vendor with an assignment on a date is never listed as available
 * and cannot be assigned to a second event on that date.
 */
@Service
@Transactional
public class StaffAssignmentService {

    private final BookingRepository bookingRepository;
    private final StaffRepository staffRepository;
    private final StaffAvailabilityRepository availabilityRepository;
    private final StaffAssignmentRepository assignmentRepository;
    private final NotificationService notificationService;

    /** Main flow step 3: staff/vendors available on the event date (optionally for one role). */
    @Transactional(readOnly = true)
    public List<StaffResponse> findAvailableStaff(Long bookingId, String role) {
        Booking booking = findBooking(bookingId);
        String roleFilter = role == null ? "" : role.trim();
        return staffRepository.findAvailable(booking.getEventDate(), roleFilter)
                .stream().map(StaffResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> listForBooking(Long bookingId) {
        findBooking(bookingId);
        return assignmentRepository.findByBookingBookingId(bookingId)
                .stream().map(AssignmentResponse::from).toList();
    }

    /** Main flow steps 4-7. Everything is validated first, so it is all-or-nothing. */
    public List<AssignmentResponse> assign(Long bookingId, AssignRequest request) {
        Booking booking = findBooking(bookingId);
        requireConfirmed(booking);

        List<StaffAssignment> toSave = new ArrayList<>();
        for (Long staffId : new LinkedHashSet<>(request.staffIds())) {
            Staff staff = findStaff(staffId);
            ensureAvailable(staff, booking.getEventDate());
            toSave.add(build(booking, staff));
        }

        List<StaffAssignment> saved = saveSafely(toSave);
        saved.forEach(a -> notificationService.notifyAssigned(a.getStaff(), booking));
        return saved.stream().map(AssignmentResponse::from).toList();
    }

    /**
     * Alternate flow 4a: replace a previously assigned staff/vendor with another.
     * The primary key is (booking_id, staff_id), so the old row is removed and a new one added.
     */
    public AssignmentResponse reassign(Long bookingId, Long currentStaffId, ReassignRequest request) {
        Booking booking = findBooking(bookingId);
        requireConfirmed(booking);

        StaffAssignment current = assignmentRepository
                .findById(new StaffAssignmentId(bookingId, currentStaffId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff/vendor " + currentStaffId + " is not assigned to booking " + bookingId));

        if (currentStaffId.equals(request.newStaffId())) {
            throw new BadRequestException("New staff/vendor is the same as the current one");
        }
        Staff newStaff = findStaff(request.newStaffId());
        ensureAvailable(newStaff, booking.getEventDate());

        Staff oldStaff = current.getStaff();
        assignmentRepository.delete(current);      // frees the original staff/vendor
        assignmentRepository.flush();

        StaffAssignment saved = saveSafely(List.of(build(booking, newStaff))).get(0);
        notificationService.notifyRemoved(oldStaff, booking);
        notificationService.notifyAssigned(newStaff, booking);
        return AssignmentResponse.from(saved);
    }

    /**
     * For Function 1 (Cancel Booking, step 5 "frees any staff or vendors assigned").
     * Note: StaffAssignment already has ON DELETE CASCADE on booking_id in the SQL script.
     */
    public void releaseForBooking(Long bookingId) {
        assignmentRepository.deleteByBookingBookingId(bookingId);
    }

    // ---------------- helpers ----------------

    private StaffAssignment build(Booking booking, Staff staff) {
        StaffAssignment a = new StaffAssignment();
        a.setId(new StaffAssignmentId(booking.getBookingId(), staff.getStaffId()));
        a.setBooking(booking);
        a.setStaff(staff);
        a.setAssignedDate(booking.getEventDate());
        return a;
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + id));
    }

    private Staff findStaff(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff/vendor not found with id " + id));
    }

    /** Precondition: booking status is Confirmed. */
    private void requireConfirmed(Booking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Staff can only be assigned to a Confirmed booking (current status: "
                    + booking.getStatus().getDbValue() + ")");
        }
    }

    private void ensureAvailable(Staff staff, LocalDate date) {
        if (availabilityRepository.existsByStaffStaffIdAndUnavailableDate(staff.getStaffId(), date)) {
            throw new ConflictException(staff.getName() + " is marked unavailable on " + date);
        }
        if (assignmentRepository.existsByStaffStaffIdAndAssignedDate(staff.getStaffId(), date)) {
            throw new ConflictException(staff.getName() + " is already assigned to an event on " + date);
        }
    }

    /** DB constraint (see sql/StaffAvailability.sql) protects against two admins assigning at once. */
    private List<StaffAssignment> saveSafely(List<StaffAssignment> assignments) {
        try {
            return assignmentRepository.saveAllAndFlush(assignments);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("A selected staff/vendor was just assigned to another event on that date");
        }
    }

    public StaffAssignmentService(BookingRepository bookingRepository, StaffRepository staffRepository, StaffAvailabilityRepository availabilityRepository, StaffAssignmentRepository assignmentRepository, NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.staffRepository = staffRepository;
        this.availabilityRepository = availabilityRepository;
        this.assignmentRepository = assignmentRepository;
        this.notificationService = notificationService;
    }
}
