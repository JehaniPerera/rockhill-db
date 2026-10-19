package com.rockhill.reception_hall.staff.controller;

import com.rockhill.reception_hall.staff.dto.UnavailabilityRequest;
import com.rockhill.reception_hall.staff.dto.UnavailabilityResponse;
import com.rockhill.reception_hall.staff.service.StaffAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Use case 2: Mark Staff/Vendor Availability. */
@RestController
@RequestMapping("/api/staff/{staffId}/unavailable-dates")
public class StaffAvailabilityController {

    private final StaffAvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<UnavailabilityResponse> markUnavailable(
            @PathVariable Long staffId, @RequestBody UnavailabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.markUnavailable(staffId, request));
    }

    @GetMapping
    public List<UnavailabilityResponse> list(@PathVariable Long staffId) {
        return availabilityService.list(staffId);
    }

    @DeleteMapping("/{date}")
    public ResponseEntity<Void> markAvailableAgain(
            @PathVariable Long staffId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        availabilityService.markAvailableAgain(staffId, date);
        return ResponseEntity.noContent().build();
    }

    public StaffAvailabilityController(StaffAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }
}
