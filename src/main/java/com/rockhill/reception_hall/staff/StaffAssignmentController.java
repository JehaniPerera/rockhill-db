package com.rockhill.reception_hall.staff;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StaffAssignmentController {

    private final StaffAssignmentService service;

    public StaffAssignmentController(StaffAssignmentService service) {
        this.service = service;
    }

    record StaffRequest(String name, String role, String type, String contact) { }
    record SelectRequest(LocalDate date, List<Integer> staffIds) { }
    record ConfirmRequest(LocalDate date, int staffId, boolean available) { }
    record ReassignRequest(LocalDate date, int oldId, int newId) { }

    @GetMapping("/staff")
    public List<Staff> allStaff() {
        return service.getAllStaff();
    }

    @PostMapping("/staff")
    public Staff addStaff(@RequestBody StaffRequest r) {
        return service.addStaff(r.name(), r.role(), r.type(), r.contact());
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable int id) {
        service.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/staff/available")
    public List<Staff> available(@RequestParam LocalDate date) {
        return service.getAvailableStaff(date, null);
    }

    @GetMapping("/staff/busy")
    public List<Staff> busy(@RequestParam LocalDate date) {
        return service.getUnavailableStaff(date);
    }

    @GetMapping("/staff/multiple")
    public List<Staff> multiple() {
        return service.getStaffAssignedToMultipleEvents();
    }

    @GetMapping("/bookings/{bid}/assignments")
    public List<AssignmentView> assignments(@PathVariable int bid, @RequestParam LocalDate date) {
        return service.getAssignmentsForBooking(bid, date);
    }

    @GetMapping("/bookings/{bid}/confirmed")
    public List<Staff> confirmed(@PathVariable int bid, @RequestParam LocalDate date) {
        return service.getConfirmedStaffForBooking(bid, date);
    }

    @PostMapping("/bookings/{bid}/select")
    public List<Staff> select(@PathVariable int bid, @RequestBody SelectRequest r) {
        return service.clientSelectStaff(bid, r.date(), r.staffIds());
    }

    @PostMapping("/bookings/{bid}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable int bid, @RequestBody ConfirmRequest r) {
        service.confirmAvailability(bid, r.date(), r.staffId(), r.available());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bookings/{bid}/reassign")
    public Map<String, Boolean> reassign(@PathVariable int bid, @RequestBody ReassignRequest r) {
        boolean swapped = service.reassign(bid, r.date(), r.oldId(), r.newId());
        return Map.of("swapped", swapped);
    }

    @DeleteMapping("/bookings/{bid}/assignments/{staffId}")
    public ResponseEntity<Void> removeAssignment(@PathVariable int bid, @PathVariable int staffId,
                                                  @RequestParam LocalDate date) {
        service.removeAssignment(bid, date, staffId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
