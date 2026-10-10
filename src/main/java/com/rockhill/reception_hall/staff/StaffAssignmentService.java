package com.rockhill.reception_hall.staff;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StaffAssignmentService {

    private List<Staff> staffList;
    private List<Assignment> assignments = new ArrayList<>();

    public StaffAssignmentService(List<Staff> staffList) {
        this.staffList = staffList;
    }

    // ---------- 1. Staff records: add / remove ----------

    /** Adds a new staff or vendor record. */
    public Staff addStaff(int id, String name, String role, String type, String contact) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        for (Staff s : staffList) {
            if (s.getId() == id) {
                throw new IllegalArgumentException("Staff id already exists: " + id);
            }
        }
        Staff staff = new Staff(id, name, role, type, contact);
        staffList.add(staff);
        return staff;
    }

    /** Removes a staff/vendor record. Blocked if they still have an active assignment. */
    public void removeStaff(int id) {
        Staff staff = findStaff(id);
        for (Assignment a : assignments) {
            if (a.getStaffId() == id && a.getStatus() != AssignmentStatus.UNAVAILABLE) {
                throw new IllegalStateException(staff.getName() + " still has an active assignment, remove that first");
            }
        }
        staffList.remove(staff);
    }

    public List<Staff> getAllStaff() {
        return staffList;
    }

    // ---------- 2. Client selects a group of staff/vendors for the booking ----------

    /**
     * Client picks the staff/vendors they want for their event.
     * This just records the request, it does NOT confirm availability yet.
     * Returns anyone who was already busy on that date (so they were not added).
     */
    public List<Staff> clientSelectStaff(int bookingId, LocalDate date, List<Integer> staffIds) {
        List<Staff> alreadyBusy = new ArrayList<>();
        for (int staffId : staffIds) {
            Staff staff = findStaff(staffId);
            if (isBusy(date, staffId)) {
                alreadyBusy.add(staff);
            } else {
                assignments.add(new Assignment(bookingId, date, staffId, AssignmentStatus.PENDING_CONFIRMATION));
            }
        }
        return alreadyBusy;
    }

    // ---------- 3. Manager contacts everyone and confirms availability ----------

    /** Manager calls one person/vendor and records whether they are actually available. */
    public void confirmAvailability(int bookingId, LocalDate date, int staffId, boolean isAvailable) {
        Assignment a = findAssignment(bookingId, date, staffId);
        if (a == null) {
            throw new IllegalArgumentException("No pending selection found for this staff member");
        }
        a.setStatus(isAvailable ? AssignmentStatus.CONFIRMED : AssignmentStatus.UNAVAILABLE);
    }

    /** Everyone still waiting on a manager phone call for this booking. */
    public List<Assignment> getPendingConfirmations(int bookingId, LocalDate date) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : assignments) {
            if (a.getBookingId() == bookingId && a.getDate().equals(date)
                    && a.getStatus() == AssignmentStatus.PENDING_CONFIRMATION) {
                result.add(a);
            }
        }
        return result;
    }

    // ---------- Shared: availability, reassign, remove ----------

    /** Staff who are free on the date (no pending or confirmed assignment). Pass role = null for all roles. */
    public List<Staff> getAvailableStaff(LocalDate date, String role) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : staffList) {
            boolean roleOk = role == null || s.getRole().equalsIgnoreCase(role);
            if (roleOk && !isBusy(date, s.getId())) {
                result.add(s);
            }
        }
        return result;
    }

    /** Staff who are busy (pending or confirmed) on the given date. */
    public List<Staff> getUnavailableStaff(LocalDate date) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : staffList) {
            if (isBusy(date, s.getId())) {
                result.add(s);
            }
        }
        return result;
    }

    /** Staff confirmed (not just pending) for a specific booking and date. */
    public List<Staff> getConfirmedStaffForBooking(int bookingId, LocalDate date) {
        List<Staff> result = new ArrayList<>();
        for (Assignment a : assignments) {
            if (a.getBookingId() == bookingId && a.getDate().equals(date)
                    && a.getStatus() == AssignmentStatus.CONFIRMED) {
                result.add(findStaff(a.getStaffId()));
            }
        }
        return result;
    }

    /** Staff who are linked (pending or confirmed) to more than one booking overall. */
    public List<Staff> getStaffAssignedToMultipleEvents() {
        Map<Integer, Set<Integer>> bookingsPerStaff = new HashMap<>();
        for (Assignment a : assignments) {
            if (a.getStatus() != AssignmentStatus.UNAVAILABLE) {
                bookingsPerStaff.computeIfAbsent(a.getStaffId(), k -> new HashSet<>()).add(a.getBookingId());
            }
        }
        List<Staff> result = new ArrayList<>();
        for (Map.Entry<Integer, Set<Integer>> entry : bookingsPerStaff.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.add(findStaff(entry.getKey()));
            }
        }
        return result;
    }

    /** Replace oldId with newId on the same booking (e.g. an unavailable vendor gets swapped). */
    public boolean reassign(int bookingId, LocalDate date, int oldId, int newId) {
        Assignment old = findAssignment(bookingId, date, oldId);
        if (old == null || isBusy(date, newId)) {
            return false;
        }
        assignments.remove(old); // old staff member becomes free again
        assignments.add(new Assignment(bookingId, date, newId, AssignmentStatus.PENDING_CONFIRMATION));
        return true;
    }

    public boolean removeAssignment(int bookingId, LocalDate date, int staffId) {
        Assignment a = findAssignment(bookingId, date, staffId);
        if (a == null) {
            return false;
        }
        assignments.remove(a);
        return true;
    }

    public List<Assignment> getAssignmentsForBooking(int bookingId, LocalDate date) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : assignments) {
            if (a.getBookingId() == bookingId && a.getDate().equals(date)) {
                result.add(a);
            }
        }
        return result;
    }

    // ---------- Helpers ----------

    private boolean isBusy(LocalDate date, int staffId) {
        for (Assignment a : assignments) {
            if (a.getDate().equals(date) && a.getStaffId() == staffId
                    && a.getStatus() != AssignmentStatus.UNAVAILABLE) {
                return true;
            }
        }
        return false;
    }

    private Assignment findAssignment(int bookingId, LocalDate date, int staffId) {
        for (Assignment a : assignments) {
            if (a.getBookingId() == bookingId && a.getDate().equals(date) && a.getStaffId() == staffId) {
                return a;
            }
        }
        return null;
    }

    private Staff findStaff(int id) {
        for (Staff s : staffList) {
            if (s.getId() == id) {
                return s;
            }
        }
        throw new IllegalArgumentException("Staff not found: " + id);
    }
}
