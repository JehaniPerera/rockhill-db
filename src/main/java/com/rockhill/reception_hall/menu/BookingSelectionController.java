package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings/{bookingId}/selections")
public class BookingSelectionController {

    @Autowired private BookingSelectionService selectionService;
    @Autowired private PricingService pricingService;

    @PostMapping("/addons/{addonId}")
    public SelectionLine addAddOn(@PathVariable Long bookingId, @PathVariable Integer addonId,
                                  @RequestParam(required = false) Integer quantity) {
        return selectionService.addAddOn(bookingId, addonId, quantity);
    }

    @GetMapping("/addons")
    public List<SelectionLine> getAddOns(@PathVariable Long bookingId) {
        return selectionService.listAddOns(bookingId);
    }

    @DeleteMapping("/addons/{addonId}")
    public ResponseEntity<Void> removeAddOn(@PathVariable Long bookingId, @PathVariable Integer addonId) {
        selectionService.removeAddOn(bookingId, addonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/decor/{decorId}")
    public SelectionLine selectDecor(@PathVariable Long bookingId, @PathVariable Integer decorId) {
        return selectionService.selectDecor(bookingId, decorId);
    }

    @GetMapping("/decor")
    public List<SelectionLine> getDecor(@PathVariable Long bookingId) {
        return selectionService.listDecor(bookingId);
    }

    @DeleteMapping("/decor/{decorId}")
    public ResponseEntity<Void> removeDecor(@PathVariable Long bookingId, @PathVariable Integer decorId) {
        selectionService.removeDecor(bookingId, decorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estimate")
    public PriceEstimate estimate(@PathVariable Long bookingId) {
        return pricingService.estimate(bookingId);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handle(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", String.valueOf(ex.getReason())));
    }
}