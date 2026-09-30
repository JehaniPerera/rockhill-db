package com.rockhill.reception_hall.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class StaffAssignmentService {

    @Autowired
    private StaffRepository staffRepo;

    @Autowired
    private AssignmentRepository assignmentRepo;

    // Not saved to the database yet - pending selections and unavailable marks
    // live here until the manager confirms someone, at which point a real row
    // gets written to the StaffAssignment table.
    // bookingId -> date -> staffId -> status
    private Map<Integer, Map<LocalDate, Map<Integer, AssignmentStatus>>> pending = new HashMap<>();

    public List<Staff> getAllStaff() {
        return staffRepo.findAll();
    }

    public Staff addStaff(String name, String role, String type, String contact) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        return staffRepo.save(new Staff(name, role, type, contact));
    }

    public void deleteStaff(int id) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + id));
        boolean hasConfirmed = assignmentRepo.findAll().stream().anyMatch(a -> a.getStaffId() == id);
        if (hasConfirmed) {
            throw new IllegalStateException(staff.getName() + " still has a confirmed assignment, remove that first");
        }
        staffRepo.deleteById(id);
    }

    // client picks a group of staff/vendors for the booking
    public List<Staff> clientSelectStaff(int bookingId, LocalDate date, List<Integer> staffIds) {
        List<Staff> alreadyBusy = new ArrayList<>();
        Map<Integer, AssignmentStatus> bucket = pendingBucket(bookingId, date);

        for (int staffId : staffIds) {
            Staff staff = staffRepo.findById(staffId)
                    .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + staffId));
            if (isBusy(date, staffId)) {
                alreadyBusy.add(staff);
            } else {
                bucket.put(staffId, AssignmentStatus.PENDING_CONFIRMATION);
            }
        }
        return alreadyBusy;
    }

    // manager calls one person and records the real answer
    public void confirmAvailability(int bookingId, LocalDate date, int staffId, boolean isAvailable) {
        Map<Integer, AssignmentStatus> bucket = pendingBucket(bookingId, date);
        if (!bucket.containsKey(staffId)) {
            throw new IllegalArgumentException("No pending selection found for this staff member");
        }
        if (isAvailable) {
            bucket.remove(staffId);
            assignmentRepo.save(new Assignment(bookingId, staffId, date)); // now saved for real
        } else {
            bucket.put(staffId, AssignmentStatus.UNAVAILABLE);
        }
    }

    // everyone linked to this booking+date, whatever their current status
    public List<AssignmentView> getAssignmentsForBooking(int bookingId, LocalDate date) {
        List<AssignmentView> result = new ArrayList<>();
        for (Assignment a : assignmentRepo.findByBookingId(bookingId)) {
            if (a.getDate().equals(date)) {
                result.add(new AssignmentView(a.getStaffId(), AssignmentStatus.CONFIRMED));
            }
        }
        pendingBucket(bookingId, date).forEach((staffId, status) ->
                result.add(new AssignmentView(staffId, status)));
        return result;
    }

    public List<Staff> getAvailableStaff(LocalDate date, String role) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : staffRepo.findAll()) {
            boolean roleOk = role == null || role.isBlank() || s.getRole().equalsIgnoreCase(role);
            if (roleOk && !isBusy(date, s.getId())) {
                result.add(s);
            }
        }
        return result;
    }

    public List<Staff> getUnavailableStaff(LocalDate date) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : staffRepo.findAll()) {
            if (isBusy(date, s.getId())) result.add(s);
        }
        return result;
    }

    public List<Staff> getConfirmedStaffForBooking(int bookingId, LocalDate date) {
        List<Staff> result = new ArrayList<>();
        for (Assignment a : assignmentRepo.findByBookingId(bookingId)) {
            if (a.getDate().equals(date)) {
                staffRepo.findById(a.getStaffId()).ifPresent(result::add);
            }
        }
        return result;
    }

    public List<Staff> getStaffAssignedToMultipleEvents() {
        Map<Integer, Set<Integer>> bookingsPerStaff = new HashMap<>();
        for (Assignment a : assignmentRepo.findAll()) {
            bookingsPerStaff.computeIfAbsent(a.getStaffId(), k -> new HashSet<>()).add(a.getBookingId());
        }
        List<Staff> result = new ArrayList<>();
        for (Map.Entry<Integer, Set<Integer>> e : bookingsPerStaff.entrySet()) {
            if (e.getValue().size() > 1) {
                staffRepo.findById(e.getKey()).ifPresent(result::add);
            }
        }
        return result;
    }

    // swap oldId for newId on the same booking+date
    public boolean reassign(int bookingId, LocalDate date, int oldId, int newId) {
        Map<Integer, AssignmentStatus> bucket = pendingBucket(bookingId, date);
        AssignmentId dbId = new AssignmentId(bookingId, oldId);

        boolean wasPendingOrUnavailable = bucket.containsKey(oldId);
        boolean wasConfirmedInDb = assignmentRepo.existsById(dbId);

        if (!wasPendingOrUnavailable && !wasConfirmedInDb) {
            return false;
        }
        if (isBusy(date, newId)) {
            return false;
        }
        if (wasConfirmedInDb) {
            assignmentRepo.deleteById(dbId);
        }
        bucket.remove(oldId);
        bucket.put(newId, AssignmentStatus.PENDING_CONFIRMATION);
        return true;
    }

    // remove someone from a booking, whatever their current status
    public boolean removeAssignment(int bookingId, LocalDate date, int staffId) {
        Map<Integer, AssignmentStatus> bucket = pendingBucket(bookingId, date);
        AssignmentId dbId = new AssignmentId(bookingId, staffId);

        boolean removed = false;
        if (bucket.remove(staffId) != null) removed = true;
        if (assignmentRepo.existsById(dbId)) {
            assignmentRepo.deleteById(dbId);
            removed = true;
        }
        return removed;
    }

    private Map<Integer, AssignmentStatus> pendingBucket(int bookingId, LocalDate date) {
        return pending
                .computeIfAbsent(bookingId, k -> new HashMap<>())
                .computeIfAbsent(date, k -> new HashMap<>());
    }

    // busy = confirmed in the database OR pending in memory, on this date, for ANY booking.
    // Being marked unavailable does NOT count as busy.
    private boolean isBusy(LocalDate date, int staffId) {
        for (Assignment a : assignmentRepo.findByDate(date)) {
            if (a.getStaffId() == staffId) return true;
        }
        for (Map<LocalDate, Map<Integer, AssignmentStatus>> byDate : pending.values()) {
            Map<Integer, AssignmentStatus> bucket = byDate.get(date);
            if (bucket != null && bucket.get(staffId) == AssignmentStatus.PENDING_CONFIRMATION) {
                return true;
            }
        }
        return false;
    }
}
