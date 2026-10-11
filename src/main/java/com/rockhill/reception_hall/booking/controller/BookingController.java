package com.rockhill.reception_hall.booking.controller;

import com.rockhill.reception_hall.booking.dto.*;
import com.rockhill.reception_hall.booking.model.*;
import com.rockhill.reception_hall.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service = service; }

    @GetMapping("/availability")
    public Map<String, Object> availability(@RequestParam LocalDate date) {
        service.requireFutureDate(date);
        boolean available = service.isDateAvailable(date);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);
        result.put("available", available);
        result.put("suggestions", available ? List.of() : service.nearestAvailableDates(date, 3));
        return result;
    }

    @PostMapping("/wedding") @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createWedding(@Valid @RequestBody BookingRequest r) {
        if (r.eventType() != EventType.WEDDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This endpoint is for wedding bookings");
        return BookingResponse.from(service.createPending(r));
    }

    @PostMapping("/event") @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createEvent(@Valid @RequestBody BookingRequest r) {
        if (r.eventType() == EventType.WEDDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use the wedding booking page for weddings");
        return BookingResponse.from(service.createPending(r));
    }

    @GetMapping("/by-phone")
    public List<BookingResponse> byPhone(@RequestParam String phone) {
        return service.byPhone(phone.trim()).stream().map(BookingResponse::from).toList();
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id) { return BookingResponse.from(service.get(id)); }

    @GetMapping
    public List<BookingResponse> list(@RequestParam(required = false) BookingStatus status,
                                      @RequestParam(required = false) Long customerId) {
        List<Booking> l = customerId != null ? service.byCustomer(customerId)
                : service.byStatus(status == null ? BookingStatus.PENDING : status);
        return l.stream().map(BookingResponse::from).toList();
    }

    @PutMapping("/{id}/package/{packageId}")
    public BookingResponse pkg(@PathVariable Long id, @PathVariable Long packageId) {
        return BookingResponse.from(service.attachPackage(id, packageId));
    }
    @PutMapping("/{id}/menu/{menuId}")
    public BookingResponse menu(@PathVariable Long id, @PathVariable Long menuId) {
        return BookingResponse.from(service.attachMenu(id, menuId));
    }

    @PostMapping("/{id}/submit")
    public BookingResponse submit(@PathVariable Long id) { return BookingResponse.from(service.submit(id)); }

    @PostMapping("/{id}/confirm")
    public BookingResponse confirm(@PathVariable Long id) { return BookingResponse.from(service.confirm(id)); }

    @PostMapping("/{id}/reject")
    public BookingResponse reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return BookingResponse.from(service.reject(id, body.get("reason")));
    }

    @PostMapping("/{id}/reschedule")
    public BookingResponse reschedule(@PathVariable Long id, @RequestParam LocalDate newDate,
                                      @RequestParam(defaultValue = "true") boolean byCustomer) {
        return BookingResponse.from(service.reschedule(id, newDate, byCustomer));
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return BookingResponse.from(service.cancel(id, body == null ? null : body.get("reason")));
    }

    @PostMapping("/{id}/complete")
    public BookingResponse complete(@PathVariable Long id,
                                    @RequestParam(defaultValue = "false") boolean venueDamaged,
                                    @RequestParam(required = false) String notes) {
        return BookingResponse.from(service.markCompleted(id, venueDamaged, notes));
    }
}