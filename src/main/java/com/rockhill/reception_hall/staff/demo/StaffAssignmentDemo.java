package com.rockhill.reception_hall.staff;

import java.time.LocalDate;
import java.util.*;

public class StaffAssignmentService {

    private static final Set<String> ROLES = Set.of(
            "Event Coordinator", "Head Waiter", "Waiter", "Waitress", "Head Cook",
            "Security", "DJ", "Decorator", "Dancing Group", "Jaya Mangala Gatha");
    private static final Set<String> TYPES = Set.of("Internal", "Vendor");

    private final List<Staff> allStaff;
    private int nextId;

    // eventDate -> (staffId -> bookingId)
    private final Map<LocalDate, Map<Integer, Integer>> assignmentsByDate = new HashMap<>();

    public StaffAssignmentService(List<Staff> initialStaff) {
        this.allStaff = new ArrayList<>(initialStaff);
        this.nextId = initialStaff.stream().mapToInt(Staff::id).max().orElse(0) + 1;
    }

    //  Staff records (add / edit / delete)

    public Staff addStaff(String name, String role, String type, String contact) {
        validateStaffDetails(name, role, type);
        Staff staff = new Staff(nextId++, name.trim(), role, type, contact);
        allStaff.add(staff);
        return staff;
    }

    public Staff updateStaff(int id, String name, String role, String type, String contact) {
        validateStaffDetails(name, role, type);
        Staff existing = findStaff(id);
        Staff updated = new Staff(id, name.trim(), role, type, contact);
        allStaff.set(allStaff.indexOf(existing), updated);
        return updated;
    }

    public void deleteStaff(int id) {
        Staff staff = findStaff(id);
        // Deleting someone with upcoming assignments would leave bookings without staff
        boolean hasAssignments = assignmentsByDate.values().stream()
                .anyMatch(day -> day.containsKey(id));
        if (hasAssignments) {
            throw new IllegalStateException(staff.name() + " has assignments. Remove them first.");
        }
        allStaff.remove(staff);
    }

    public List<Staff> getAllStaff() {
        return List.copyOf(allStaff);
    }

    //  Availability (step 3) with optional role filter 

    public List<Staff> getAvailableStaff(LocalDate date) {
        return getAvailableStaff(date, null);
    }

    /** Role can be null or blank to skip filtering. */
    public List<Staff> getAvailableStaff(LocalDate date, String role) {
        Objects.requireNonNull(date, "date is required");
        Map<Integer, Integer> busy = assignmentsByDate.getOrDefault(date, Map.of());
        return allStaff.stream()
                .filter(s -> !busy.containsKey(s.id()))
                .filter(s -> role == null || role.isBlank() || s.role().equalsIgnoreCase(role))
                .toList();
    }

    // Assign (steps 4-6) 

    /**
     * Assigns the selected staff. Busy staff are reported back
     * instead of failing the whole request.
     */
    public AssignResult assignStaff(int bookingId, LocalDate date, Collection<Integer> selectedIds) {
        Objects.requireNonNull(date, "date is required");
        if (selectedIds == null || selectedIds.isEmpty()) {
            throw new IllegalArgumentException("No staff selected");
        }

        Map<Integer, Integer> busy = assignmentsByDate.computeIfAbsent(date, d -> new HashMap<>());
        List<Staff> assigned = new ArrayList<>();
        List<Staff> unavailable = new ArrayList<>();

        // LinkedHashSet drops duplicate ids but keeps the selection order
        for (int id : new LinkedHashSet<>(selectedIds)) {
            Staff staff = findStaff(id);
            if (busy.containsKey(id)) {
                unavailable.add(staff);
            } else {
                busy.put(id, bookingId);
                assigned.add(staff);
            }
        }
        return new AssignResult(assigned, unavailable);
    }

    //  Reassign (alternative flow 4a) 

    public boolean reassignStaff(int bookingId, LocalDate date, int oldId, int newId) {
        Map<Integer, Integer> busy = assignmentsByDate.getOrDefault(date, new HashMap<>());
        boolean oldIsOnThisBooking = Integer.valueOf(bookingId).equals(busy.get(oldId));
        if (!oldIsOnThisBooking || busy.containsKey(newId)) {
            return false;
        }
        findStaff(newId); // validates the new id exists
        busy.remove(oldId); // the previous staff member becomes free again
        busy.put(newId, bookingId);
        return true;
    }

    //  Remove an assignment 

    /** Frees the staff member for that date. Returns false if they were not on this booking. */
    public boolean removeAssignment(int bookingId, LocalDate date, int staffId) {
        Map<Integer, Integer> busy = assignmentsByDate.get(date);
        if (busy == null || !Integer.valueOf(bookingId).equals(busy.get(staffId))) {
            return false;
        }
        busy.remove(staffId);
        return true;
    }

    public List<Staff> getAssignedStaff(int bookingId, LocalDate date) {
        List<Staff> result = new ArrayList<>();
        assignmentsByDate.getOrDefault(date, Map.of()).forEach((staffId, bId) -> {
            if (bId == bookingId) {
                result.add(findStaff(staffId));
            }
        });
        return result;
    }

    // Helpers

    private void validateStaffDetails(String name, String role, String type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (!ROLES.contains(role)) {
            throw new IllegalArgumentException("Unknown role: " + role);
        }
        if (!TYPES.contains(type)) {
            throw new IllegalArgumentException("Type must be Internal or Vendor");
        }
    }

    private Staff findStaff(int id) {
        return allStaff.stream()
                .filter(s -> s.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Staff not found: " + id));
    }
}
