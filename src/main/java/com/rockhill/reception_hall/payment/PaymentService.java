package com.rockhill.reception_hall.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private final JdbcTemplate jdbcTemplate;
    private static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("20000.00");

    public PaymentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Payment recordDeposit(int bookingId) {
        return insertPayment(bookingId, DEPOSIT_AMOUNT, Payment.PaymentType.DEPOSIT,
                Payment.PaymentStatus.PARTIALLY_PAID, Payment.RefundStatus.NA);
    }

    public Payment recordBalance(int bookingId, BigDecimal totalPrice) {
        BigDecimal balance = totalPrice.subtract(DEPOSIT_AMOUNT);
        return insertPayment(bookingId, balance, Payment.PaymentType.BALANCE,
                Payment.PaymentStatus.FULLY_PAID, Payment.RefundStatus.NA);
    }

    private Payment insertPayment(int bookingId, BigDecimal amount, Payment.PaymentType type,
                                  Payment.PaymentStatus status, Payment.RefundStatus refundStatus) {
        String sql = "INSERT INTO Payment (booking_id, amount, payment_type, payment_date, payment_status, refund_status) VALUES (?, ?, ?, ?, ?, ?)";
        LocalDate today = LocalDate.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bookingId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, type.dbValue());
            ps.setDate(4, Date.valueOf(today));
            ps.setString(5, status.dbValue());
            ps.setString(6, refundStatus.dbValue());
            return ps;
        }, keyHolder);

        int newId = keyHolder.getKey().intValue();
        return new Payment(newId, bookingId, amount, type, today, status, refundStatus);
    }

    public Invoice generateInvoice(int bookingId) {
        BigDecimal totalPrice = jdbcTemplate.queryForObject(
                "SELECT total_price FROM Booking WHERE booking_id = ?",
                BigDecimal.class, bookingId);

        String sql = "INSERT INTO Invoice (booking_id, total_amount, issue_date) VALUES (?, ?, ?)";
        LocalDate today = LocalDate.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bookingId);
            ps.setBigDecimal(2, totalPrice);
            ps.setDate(3, Date.valueOf(today));
            return ps;
        }, keyHolder);

        int newId = keyHolder.getKey().intValue();
        return new Invoice(newId, bookingId, totalPrice, today);
    }

    public void processRefund(int bookingId, boolean venueDamaged, boolean cancelled) {
        Payment.RefundStatus newStatus = (cancelled || venueDamaged)
                ? Payment.RefundStatus.FORFEITED
                : Payment.RefundStatus.REFUNDED;

        jdbcTemplate.update(
                "UPDATE Payment SET refund_status = ? WHERE booking_id = ? AND payment_type = 'Deposit'",
                newStatus.dbValue(), bookingId);
    }

    public List<Map<String, Object>> getAllPayments() {
        return jdbcTemplate.queryForList("SELECT * FROM Payment ORDER BY payment_id");
    }
}