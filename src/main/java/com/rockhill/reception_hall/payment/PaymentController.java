package com.rockhill.reception_hall.payment;

import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

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

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }
}
