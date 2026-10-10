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

    // true when the event is less than 14 days away (the client's "pay everything at once" rule)
    public boolean isWithinBalanceWindow(int bookingId) {
        Integer days = jdbc.queryForObject(
                "SELECT DATEDIFF(event_date, CURDATE()) FROM Booking WHERE booking_id = ?",
                Integer.class, bookingId);
        return days != null && days < 14;
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

    public void confirmBooking(int bookingId) {
        jdbc.update("UPDATE Booking SET status = 'Confirmed' WHERE booking_id = ? AND status = 'Pending'", bookingId);
    }

    public void updateDepositRefundStatus(int bookingId, Payment.RefundStatus status) {
        jdbc.update("UPDATE Payment SET refund_status = ? WHERE booking_id = ? AND payment_type = 'Deposit'",
                status.dbValue(), bookingId);
    }

    public List<Map<String, Object>> findBookingSummaries() {
        return jdbc.queryForList(
                "SELECT b.booking_id, c.name AS customer_name, b.event_type, " +
                        "DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, " +
                        "DATE_FORMAT(GREATEST(DATE_SUB(b.event_date, INTERVAL 14 DAY), CURDATE()), '%Y-%m-%d') AS balance_due_date, " +
                        "(DATEDIFF(b.event_date, CURDATE()) < 14) AS late_booking, " +
                        "b.guest_count, b.status, b.total_price, b.venue_damaged, " +
                        "(SELECT COUNT(*) FROM Payment p WHERE p.booking_id = b.booking_id AND p.payment_type = 'Deposit') AS deposit_paid, " +
                        "(SELECT COUNT(*) FROM Payment p WHERE p.booking_id = b.booking_id AND p.payment_type = 'Balance') AS balance_paid, " +
                        "(SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.booking_id = b.booking_id) AS paid_total, " +
                        "(SELECT COUNT(*) FROM Invoice i WHERE i.booking_id = b.booking_id) AS has_invoice " +
                        "FROM Booking b JOIN Customer c ON b.customer_id = c.customer_id " +
                        "ORDER BY b.booking_id");
    }

    public Optional<Map<String, Object>> findInvoiceDetails(int bookingId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT i.invoice_id, i.total_amount, DATE_FORMAT(i.issue_date, '%Y-%m-%d') AS issue_date, " +
                        "b.booking_id, b.event_type, DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, b.guest_count, " +
                        "c.name AS customer_name, c.phone AS customer_phone " +
                        "FROM Invoice i " +
                        "JOIN Booking b ON i.booking_id = b.booking_id " +
                        "JOIN Customer c ON b.customer_id = c.customer_id " +
                        "WHERE i.booking_id = ?", bookingId);
        return rows.stream().findFirst();
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