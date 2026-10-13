package com.rockhill.hallbooking.controller;

import com.rockhill.hallbooking.dto.DamageReportRequest;
import com.rockhill.hallbooking.dto.DamageReportResponse;
import com.rockhill.hallbooking.service.DamageReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Staff admin: was the hall damaged after the event? (read by the Payment function) */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DamageReportController {

    private final DamageReportService damageReportService;

    @GetMapping("/bookings/{bookingId}/damage-report")
    public DamageReportResponse get(@PathVariable Long bookingId) {
        return damageReportService.get(bookingId);
    }

    @PostMapping("/bookings/{bookingId}/damage-report")
    public ResponseEntity<DamageReportResponse> create(@PathVariable Long bookingId,
                                                       @Valid @RequestBody DamageReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(damageReportService.create(bookingId, request));
    }

    @PutMapping("/bookings/{bookingId}/damage-report")
    public DamageReportResponse update(@PathVariable Long bookingId,
                                       @Valid @RequestBody DamageReportRequest request) {
        return damageReportService.update(bookingId, request);
    }

    @GetMapping("/damage-reports")
    public List<DamageReportResponse> listAll() {
        return damageReportService.listAll();
    }
}
