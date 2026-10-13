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

    // true once the administrator has approved the date for this booking
    public boolean isAvailabilityApproved(int bookingId) {
        Boolean approved = jdbc.queryForObject(
                "SELECT availability_approved FROM Booking WHERE booking_id = ?",
                Boolean.class, bookingId);
        return approved != null && approved;
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

    // true when the deposit has already been refunded or forfeited
    public boolean isRefundProcessed(int bookingId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Payment WHERE booking_id = ? AND payment_type = 'Deposit' AND refund_status <> 'N/A'",
                Integer.class, bookingId);
        return count != null && count > 0;
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
        jdbc.update("UPDATE Payment SET refund_status = ?, refund_date = CURDATE() WHERE booking_id = ? AND payment_type = 'Deposit'",
                status.dbValue(), bookingId);
    }

    // customerId == null: every booking (administrator); otherwise only that customer's bookings
    public List<Map<String, Object>> findBookingSummaries(Long customerId) {
        String where = customerId == null ? "" : "WHERE b.customer_id = ? ";
        String sql =
                "SELECT b.booking_id, c.name AS customer_name, b.event_type, " +
                        "DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, " +
                        "DATE_FORMAT(GREATEST(DATE_SUB(b.event_date, INTERVAL 14 DAY), CURDATE()), '%Y-%m-%d') AS balance_due_date, " +
                        "(DATEDIFF(b.event_date, CURDATE()) < 14) AS late_booking, " +
                        "DATE_FORMAT(DATE_SUB(b.event_date, INTERVAL 14 DAY), '%Y-%m-%d') AS due_date, " +
                        "(b.status = 'Confirmed' AND CURDATE() > DATE_SUB(b.event_date, INTERVAL 14 DAY) " +
                        "AND NOT EXISTS (SELECT 1 FROM Payment x WHERE x.booking_id = b.booking_id AND x.payment_type = 'Balance')) AS is_overdue, " +
                        "b.guest_count, b.status, b.total_price, b.venue_damaged, b.availability_approved, " +
                        "(SELECT p.refund_status FROM Payment p WHERE p.booking_id = b.booking_id AND p.payment_type = 'Deposit') AS deposit_refund_status, " +
                        "(SELECT COUNT(*) FROM Payment p WHERE p.booking_id = b.booking_id AND p.payment_type = 'Deposit') AS deposit_paid, " +
                        "(SELECT COUNT(*) FROM Payment p WHERE p.booking_id = b.booking_id AND p.payment_type = 'Balance') AS balance_paid, " +
                        "(SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.booking_id = b.booking_id) AS paid_total, " +
                        "(SELECT COUNT(*) FROM Invoice i WHERE i.booking_id = b.booking_id) AS has_invoice " +
                        "FROM Booking b JOIN Customer c ON b.customer_id = c.customer_id " +
                        where + "ORDER BY b.booking_id";
        return customerId == null ? jdbc.queryForList(sql) : jdbc.queryForList(sql, customerId);
    }

    // who owns this booking (used to stop a customer opening someone else's booking)
    public Optional<Integer> findBookingOwner(int bookingId) {
        List<Integer> rows = jdbc.query("SELECT customer_id FROM Booking WHERE booking_id = ?",
                (rs, i) -> rs.getInt("customer_id"), bookingId);
        return rows.stream().findFirst();
    }

    public Optional<Integer> findPaymentBooking(int paymentId) {
        List<Integer> rows = jdbc.query("SELECT booking_id FROM Payment WHERE payment_id = ?",
                (rs, i) -> rs.getInt("booking_id"), paymentId);
        return rows.stream().findFirst();
    }

    // bookings that could need a balance call: a deposit has been paid and the booking is Confirmed or Completed.
    // PaymentService.getBalanceReminders() works out the dates and the Pending / Overdue / Completed status.
    public List<Map<String, Object>> findReminderCandidates() {
        return jdbc.queryForList(
                "SELECT b.booking_id, c.name AS customer_name, c.phone AS customer_phone, b.status, b.total_price, " +
                        "DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, " +
                        "(SELECT DATE_FORMAT(MIN(d.payment_date), '%Y-%m-%d') FROM Payment d " +
                        "WHERE d.booking_id = b.booking_id AND d.payment_type = 'Deposit') AS deposit_date, " +
                        "(SELECT DATE_FORMAT(MIN(p.payment_date), '%Y-%m-%d') FROM Payment p " +
                        "WHERE p.booking_id = b.booking_id AND p.payment_type = 'Balance') AS balance_paid_date, " +
                        "(SELECT COUNT(*) FROM PaymentProof pp WHERE pp.booking_id = b.booking_id " +
                        "AND pp.payment_type = 'Balance' AND pp.status = 'Pending Approval') AS proof_pending " +
                        "FROM Booking b JOIN Customer c ON b.customer_id = c.customer_id " +
                        "WHERE b.status IN ('Confirmed', 'Completed') " +
                        "AND EXISTS (SELECT 1 FROM Payment x WHERE x.booking_id = b.booking_id AND x.payment_type = 'Deposit') " +
                        "ORDER BY b.event_date");
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

    // one payment with its customer and booking details, for the receipt page
    public Optional<Map<String, Object>> findReceipt(int paymentId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT p.payment_id, CONCAT('RCPT-', LPAD(p.payment_id, 5, '0')) AS receipt_no, " +
                        "p.booking_id, p.amount, p.payment_type, p.payment_status, " +
                        "DATE_FORMAT(p.payment_date, '%Y-%m-%d') AS payment_date, " +
                        "b.event_type, DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, " +
                        "c.name AS customer_name, c.phone AS customer_phone, " +
                        "CASE WHEN b.status = 'Cancelled' OR EXISTS (SELECT 1 FROM Payment x WHERE x.booking_id = b.booking_id AND x.payment_type = 'Balance') " +
                        "THEN 0 ELSE b.total_price END AS balance_remaining " +
                        "FROM Payment p " +
                        "JOIN Booking b ON p.booking_id = b.booking_id " +
                        "JOIN Customer c ON b.customer_id = c.customer_id " +
                        "WHERE p.payment_id = ?", paymentId);
        return rows.stream().findFirst();
    }

    public List<Map<String, Object>> findHistory(int bookingId) {
        return jdbc.queryForList(
                "SELECT payment_id, CONCAT('RCPT-', LPAD(payment_id, 5, '0')) AS receipt_no, " +
                        "booking_id, amount, payment_type, " +
                        "DATE_FORMAT(payment_date, '%Y-%m-%d') AS payment_date, " +
                        "payment_status, refund_status " +
                        "FROM Payment WHERE booking_id = ? ORDER BY payment_id", bookingId);
    }

    public List<Map<String, Object>> findAll() {
        return jdbc.queryForList(
                "SELECT payment_id, CONCAT('RCPT-', LPAD(payment_id, 5, '0')) AS receipt_no, " +
                        "booking_id, amount, payment_type, " +
                        "DATE_FORMAT(payment_date, '%Y-%m-%d') AS payment_date, " +
                        "payment_status, refund_status FROM Payment ORDER BY payment_id");
    }
}