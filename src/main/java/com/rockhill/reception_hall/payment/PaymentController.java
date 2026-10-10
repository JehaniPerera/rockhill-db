package com.rockhill.reception_hall.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/deposit/{bookingId}")
    public Payment payDeposit(@PathVariable int bookingId) {
        return paymentService.recordDeposit(bookingId);
    }

    @PostMapping("/balance/{bookingId}")
    public Payment payBalance(@PathVariable int bookingId) {
        return paymentService.recordBalance(bookingId);
    }

    @PostMapping("/full/{bookingId}")
    public String payFull(@PathVariable int bookingId) {
        return paymentService.recordFullPayment(bookingId);
    }

    @PostMapping("/invoice/{bookingId}")
    public Invoice createInvoice(@PathVariable int bookingId) {
        return paymentService.generateInvoice(bookingId);
    }

    @GetMapping("/invoice/{bookingId}")
    public Map<String, Object> viewInvoice(@PathVariable int bookingId) {
        return paymentService.getInvoiceDetails(bookingId);
    }

    @PostMapping("/refund/{bookingId}")
    public String refund(@PathVariable int bookingId) {
        return paymentService.processRefund(bookingId);
    }

    @GetMapping("/bookings")
    public List<Map<String, Object>> getBookings() {
        return paymentService.getBookings();
    }

    @GetMapping("/history/{bookingId}")
    public List<Map<String, Object>> getHistory(@PathVariable int bookingId) {
        return paymentService.getPaymentHistory(bookingId);
    }

    @GetMapping
    public List<Map<String, Object>> getAllPayments() {
        return paymentService.getAllPayments();
    }

    // every business-rule error (wrong status, already paid, booking not found) becomes a 400 with a readable message
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleBusinessRule(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}