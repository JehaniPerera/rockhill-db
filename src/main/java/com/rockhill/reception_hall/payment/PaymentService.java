package com.rockhill.reception_hall.payment;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final List<Payment> payments = new ArrayList<>();
    private int nextId = 1;

    private static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("20000.00");

    public Payment recordDeposit(int bookingId) {
        Payment payment = new Payment(nextId++, bookingId, DEPOSIT_AMOUNT,
                Payment.PaymentType.DEPOSIT, LocalDate.now(),
                Payment.PaymentStatus.PARTIALLY_PAID, Payment.RefundStatus.NA);
        payments.add(payment);
        return payment;
    }

    public Payment recordBalance(int bookingId, BigDecimal totalPrice) {
        BigDecimal balance = totalPrice.subtract(DEPOSIT_AMOUNT);
        Payment payment = new Payment(nextId++, bookingId, balance,
                Payment.PaymentType.BALANCE, LocalDate.now(),
                Payment.PaymentStatus.FULLY_PAID, Payment.RefundStatus.NA);
        payments.add(payment);
        return payment;
    }

    public List<Payment> getAllPayments() {
        return payments;
    }
}
