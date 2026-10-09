package com.rockhill.reception_hall.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbc;

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<BookingInfo> findBooking(int bookingId) {
        List<BookingInfo> rows = jdbc.query(
                "SELECT booking_id, status, total_price, venue_damaged FROM Booking WHERE booking_id = ?",
                (rs, i) -> new BookingInfo(rs.getInt("booking_id"), rs.getString("status"),
                        rs.getBigDecimal("total_price"), rs.getBoolean("venue_damaged")),
                bookingId);
        return rows.stream().findFirst();
    }

    public int countPayments(int bookingId, Payment.PaymentType type) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Payment WHERE booking_id = ? AND payment_type = ?",
                Integer.class, bookingId, type.dbValue());
        return count == null ? 0 : count;
    }

    public Payment save(Payment p) {
        String sql = "INSERT INTO Payment (booking_id, amount, payment_type, payment_date, payment_status, refund_status) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getBookingId());
            ps.setBigDecimal(2, p.getAmount());
            ps.setString(3, p.getPaymentType().dbValue());
            ps.setDate(4, Date.valueOf(p.getPaymentDate()));
            ps.setString(5, p.getPaymentStatus().dbValue());
            ps.setString(6, p.getRefundStatus().dbValue());
            return ps;
        }, keys);
        p.setPaymentId(keys.getKey().intValue());
        return p;
    }

    public void updateBookingStatus(int bookingId, String status) {
        jdbc.update("UPDATE Booking SET status = ? WHERE booking_id = ?", status, bookingId);
    }

    public void updateDepositRefundStatus(int bookingId, Payment.RefundStatus status) {
        jdbc.update("UPDATE Payment SET refund_status = ? WHERE booking_id = ? AND payment_type = 'Deposit'",
                status.dbValue(), bookingId);
    }

    public List<Map<String, Object>> findBookingSummaries() {
        return jdbc.queryForList(
                "SELECT b.booking_id, c.name AS customer_name, b.event_type, " +
                        "DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, " +
                        "DATE_FORMAT(DATE_SUB(b.event_date, INTERVAL 14 DAY), '%Y-%m-%d') AS balance_due_date, " +
                        "b.guest_count, b.status, b.total_price, b.venue_damaged " +
                        "FROM Booking b JOIN Customer c ON b.customer_id = c.customer_id " +
                        "ORDER BY b.booking_id");
    }

    public List<Map<String, Object>> findHistory(int bookingId) {
        return jdbc.queryForList(
                "SELECT payment_id, booking_id, amount, payment_type, " +
                        "DATE_FORMAT(payment_date, '%Y-%m-%d') AS payment_date, " +
                        "payment_status, refund_status " +
                        "FROM Payment WHERE booking_id = ? ORDER BY payment_id", bookingId);
    }

    public List<Map<String, Object>> findAll() {
        return jdbc.queryForList(
                "SELECT payment_id, booking_id, amount, payment_type, " +
                        "DATE_FORMAT(payment_date, '%Y-%m-%d') AS payment_date, " +
                        "payment_status, refund_status FROM Payment ORDER BY payment_id");
    }
}
