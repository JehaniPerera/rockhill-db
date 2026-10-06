package com.rockhill.reception_hall.payment;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
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
    public Payment payBalance(@PathVariable int bookingId, @RequestParam BigDecimal totalPrice) {
        return paymentService.recordBalance(bookingId, totalPrice);
    }

    @PostMapping("/invoice/{bookingId}")
    public Invoice createInvoice(@PathVariable int bookingId) {
        return paymentService.generateInvoice(bookingId);
    }

    @PostMapping("/refund/{bookingId}")
    public String refund(@PathVariable int bookingId,
                         @RequestParam boolean venueDamaged,
                         @RequestParam boolean cancelled) {
        paymentService.processRefund(bookingId, venueDamaged, cancelled);
        return "Refund status updated";
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
}