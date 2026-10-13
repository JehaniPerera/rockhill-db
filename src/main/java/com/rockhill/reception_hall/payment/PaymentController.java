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

    // Payments are no longer recorded directly: a customer uploads a receipt (PaymentProofController)
    // and the payment is recorded only when the administrator approves it.

    @PostMapping("/invoice/{bookingId}")
    public Invoice createInvoice(@PathVariable int bookingId) {
        return paymentService.generateInvoice(bookingId);
    }

    @GetMapping("/invoice/{bookingId}")
    public Map<String, Object> viewInvoice(@PathVariable int bookingId) {
        return paymentService.getInvoiceDetails(bookingId);
    }

    @GetMapping("/receipt/{paymentId}")
    public Map<String, Object> viewReceipt(@PathVariable int paymentId) {
        return paymentService.getReceipt(paymentId);
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