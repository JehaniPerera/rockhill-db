package com.rockhill.reception_hall.staff.controller;

import com.rockhill.reception_hall.staff.dto.DamageReportRequest;
import com.rockhill.reception_hall.staff.dto.DamageReportResponse;
import com.rockhill.reception_hall.staff.service.DamageReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Staff admin: was the hall damaged after the event? (read by the Payment function) */
@RestController
@RequestMapping("/api/staff")   // under /api/staff/** so the demo's existing admin-only rule covers it
public class DamageReportController {

    private final DamageReportService damageReportService;

    @GetMapping("/bookings/{bookingId}/damage-report")
    public DamageReportResponse get(@PathVariable Long bookingId) {
        return damageReportService.get(bookingId);
    }

    @PostMapping("/bookings/{bookingId}/damage-report")
    public ResponseEntity<DamageReportResponse> create(@PathVariable Long bookingId,
                                                       @RequestBody DamageReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(damageReportService.create(bookingId, request));
    }

    @PutMapping("/bookings/{bookingId}/damage-report")
    public DamageReportResponse update(@PathVariable Long bookingId,
                                       @RequestBody DamageReportRequest request) {
        return damageReportService.update(bookingId, request);
    }

    @GetMapping("/damage-reports")
    public List<DamageReportResponse> listAll() {
        return damageReportService.listAll();
    }

    public DamageReportController(DamageReportService damageReportService) {
        this.damageReportService = damageReportService;
    }
}
