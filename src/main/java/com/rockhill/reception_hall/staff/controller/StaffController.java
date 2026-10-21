package com.rockhill.reception_hall.staff.controller;

import com.rockhill.reception_hall.staff.dto.StaffRequest;
import com.rockhill.reception_hall.staff.dto.StaffResponse;
import com.rockhill.reception_hall.staff.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Use case 1: Manage Staff/Vendor Records. */
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public ResponseEntity<StaffResponse> create(@RequestBody StaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.create(request));
    }

    @GetMapping
    public List<StaffResponse> list() {
        return staffService.list();
    }

    @GetMapping("/{id}")
    public StaffResponse get(@PathVariable Long id) {
        return staffService.get(id);
    }

    @PutMapping("/{id}")
    public StaffResponse update(@PathVariable Long id, @RequestBody StaffRequest request) {
        return staffService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        staffService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }
}
