package com.rockhill.hallbooking.controller;

import com.rockhill.hallbooking.dto.UnavailabilityRequest;
import com.rockhill.hallbooking.dto.UnavailabilityResponse;
import com.rockhill.hallbooking.service.StaffAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Use case 2: Mark Staff/Vendor Availability. */
@RestController
@RequestMapping("/api/staff/{staffId}/unavailable-dates")
@RequiredArgsConstructor
public class StaffAvailabilityController {

    private final StaffAvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<UnavailabilityResponse> markUnavailable(
            @PathVariable Long staffId, @Valid @RequestBody UnavailabilityRequest request) {
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
}
