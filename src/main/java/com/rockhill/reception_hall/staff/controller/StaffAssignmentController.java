package com.rockhill.reception_hall.staff.controller;

import com.rockhill.reception_hall.staff.dto.AssignRequest;
import com.rockhill.reception_hall.staff.dto.AssignmentResponse;
import com.rockhill.reception_hall.staff.dto.ReassignRequest;
import com.rockhill.reception_hall.staff.dto.StaffResponse;
import com.rockhill.reception_hall.staff.service.StaffAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Use case 3: Assign Staff/Vendor to Booking. */
@RestController
@RequestMapping("/api/bookings/{bookingId}/assignments")
public class StaffAssignmentController {

    private final StaffAssignmentService assignmentService;

    @GetMapping
    public List<AssignmentResponse> list(@PathVariable Long bookingId) {
        return assignmentService.listForBooking(bookingId);
    }

    @GetMapping("/available-staff")
    public List<StaffResponse> availableStaff(@PathVariable Long bookingId,
                                              @RequestParam(required = false) String role) {
        return assignmentService.findAvailableStaff(bookingId, role);
    }

    @PostMapping
    public ResponseEntity<List<AssignmentResponse>> assign(@PathVariable Long bookingId,
                                                           @RequestBody AssignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.assign(bookingId, request));
    }

    /** Replace the staff/vendor {staffId} on this booking with request.newStaffId. */
    @PutMapping("/{staffId}")
    public AssignmentResponse reassign(@PathVariable Long bookingId, @PathVariable Long staffId,
                                       @RequestBody ReassignRequest request) {
        return assignmentService.reassign(bookingId, staffId, request);
    }

    public StaffAssignmentController(StaffAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }
}
