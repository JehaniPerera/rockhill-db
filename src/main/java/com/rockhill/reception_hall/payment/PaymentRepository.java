package com.rockhill.reception_hall.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PaymentRepository {

    // total of the approved Balance payments (installments) of booking b
    private static final String BALANCE_PAID_SQL =
            "COALESCE((SELECT SUM(bp.amount) FROM Payment bp WHERE bp.booking_id = b.booking_id AND bp.payment_type = 'Balance'), 0)";

    // The booking table (Function 1) no longer stores a total price, so it is worked out here from the
    // booking's package/menu and its add-on and decor choices (Function 2). Use it on a booking aliased "b".
    //   wedding : package price for the guest-count bracket
    //   other   : menu price per guest x guests, plus the Rs. 35,000 hall charge when under 150 guests
    //   both    : + add-ons and decor (quantity x charged_amount)
    static final String TOTAL_PRICE_SQL =
            "(COALESCE((SELECT t.total_price FROM wedding_booking wb JOIN package_pricing_tier t " +
                    "ON t.package_id = wb.package_id AND b.guest_count BETWEEN t.min_guests AND t.max_guests " +
                    "WHERE wb.booking_id = b.booking_id LIMIT 1), 0) " +
                    "+ COALESCE((SELECT m.price_per_guest * b.guest_count FROM other_event_booking ob " +
                    "JOIN menu m ON m.menu_id = ob.menu_id WHERE ob.booking_id = b.booking_id), 0) " +
                    "+ CASE WHEN UPPER(b.event_type) = 'OTHER' AND b.guest_count < 150 THEN 35000 ELSE 0 END " +
                    "+ COALESCE((SELECT SUM(a.quantity * a.charged_amount) FROM booking_add_on a WHERE a.booking_id = b.booking_id), 0) " +
                    "+ COALESCE((SELECT SUM(d.quantity * d.charged_amount) FROM booking_decor_selection d WHERE d.booking_id = b.booking_id), 0))";

    // 'WEDDING' in the database -> 'Wedding' on the pages
    static final String EVENT_TYPE_SQL =
            "CONCAT(UPPER(LEFT(b.event_type, 1)), LOWER(SUBSTRING(b.event_type, 2)))";

    private final JdbcTemplate jdbc;

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<BookingInfo> findBooking(int bookingId) {
        List<BookingInfo> rows = jdbc.query(
                "SELECT b.booking_id, b.status, " + TOTAL_PRICE_SQL + " AS total_price, b.venue_damaged FROM Booking b WHERE b.booking_id = ?",
                (rs, i) -> new BookingInfo(rs.getInt("booking_id"), normalizeStatus(rs.getString("status")),
                        rs.getBigDecimal("total_price"), rs.getBoolean("venue_damaged")),
                bookingId);
        return rows.stream().findFirst();
    }

    // the database may hold 'CONFIRMED', 'confirmed' or 'Confirmed': always hand out 'Confirmed'
    static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return status;
        }
        String t = status.trim();
        return t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase();
    }

    // true once the administrator has approved the date for this booking
    public boolean isAvailabilityApproved(int bookingId) {
        Boolean approved = jdbc.queryForObject(
                "SELECT (confirmed_at IS NOT NULL) FROM Booking WHERE booking_id = ?",
                Boolean.class, bookingId);
        return approved != null && approved;
    }

    // true when today is after the balance due date (the event is less than 14 days away)
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

    // total amount of the payments of one type (for Balance: all installments added together)
    public BigDecimal sumPayments(int bookingId, Payment.PaymentType type) {
        BigDecimal sum = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM Payment WHERE booking_id = ? AND payment_type = ?",
                BigDecimal.class, bookingId, type.dbValue());
        return sum == null ? BigDecimal.ZERO : sum;
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
        jdbc.update("UPDATE Booking SET status = 'CONFIRMED', deposit_received_at = NOW(), deposit_confirmed_by = 'AUTO', " +
                "last_activity_at = NOW() WHERE booking_id = ? AND UPPER(status) = 'PENDING'", bookingId);
    }

    public void updateDepositRefundStatus(int bookingId, Payment.RefundStatus status) {
        jdbc.update("UPDATE Payment SET refund_status = ?, refund_date = CURDATE() WHERE booking_id = ? AND payment_type = 'Deposit'",
                status.dbValue(), bookingId);
    }

    // cancelled booking: the balance installments the customer paid so far are given back
    public void updateBalanceRefundStatus(int bookingId, Payment.RefundStatus status) {
        jdbc.update("UPDATE Payment SET refund_status = ?, refund_date = CURDATE() WHERE booking_id = ? AND payment_type = 'Balance'",
                status.dbValue(), bookingId);
    }

    public List<Map<String, Object>> findBookingSummaries() {
        return jdbc.queryForList(
                "SELECT b.booking_id AS booking_id, c.name AS customer_name, " + EVENT_TYPE_SQL + " AS event_type, " +
                        "DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, " +
                        "DATE_FORMAT(GREATEST(DATE_SUB(b.event_date, INTERVAL 14 DAY), CURDATE()), '%Y-%m-%d') AS balance_due_date, " +
                        "(DATEDIFF(b.event_date, CURDATE()) < 14) AS late_booking, " +
                        "DATE_FORMAT(DATE_SUB(b.event_date, INTERVAL 14 DAY), '%Y-%m-%d') AS due_date, " +
                        "(b.status IN ('CONFIRMED','COMPLETED') AND CURDATE() > DATE_SUB(b.event_date, INTERVAL 14 DAY) " +
                        "AND " + BALANCE_PAID_SQL + " < " + TOTAL_PRICE_SQL + ") AS is_overdue, " +
                        "b.guest_count, CONCAT(UPPER(LEFT(b.status, 1)), LOWER(SUBSTRING(b.status, 2))) AS status, " + TOTAL_PRICE_SQL + " AS total_price, b.venue_damaged, (b.confirmed_at IS NOT NULL) AS availability_approved, " +
                        "(SELECT p.refund_status FROM Payment p WHERE p.booking_id = b.booking_id AND p.payment_type = 'Deposit' ORDER BY p.payment_id LIMIT 1) AS deposit_refund_status, " +
                        "(SELECT COUNT(*) FROM Payment p WHERE p.booking_id = b.booking_id AND p.payment_type = 'Deposit') AS deposit_paid, " +
                        "(" + BALANCE_PAID_SQL + " >= " + TOTAL_PRICE_SQL + ") AS balance_paid, " +
                        BALANCE_PAID_SQL + " AS balance_paid_amount, " +
                        "GREATEST(" + TOTAL_PRICE_SQL + " - " + BALANCE_PAID_SQL + ", 0) AS balance_remaining, " +
                        "(SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.booking_id = b.booking_id) AS paid_total, " +
                        "(SELECT COUNT(*) FROM Invoice i WHERE i.booking_id = b.booking_id) AS has_invoice " +
                        "FROM Booking b JOIN Customer c ON b.customer_id = c.id " +
                        "ORDER BY b.booking_id");
    }

    public Optional<Map<String, Object>> findInvoiceDetails(int bookingId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT i.invoice_id, i.total_amount, DATE_FORMAT(i.issue_date, '%Y-%m-%d') AS issue_date, " +
                        "b.booking_id AS booking_id, " + EVENT_TYPE_SQL + " AS event_type, DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, b.guest_count, " +
                        "c.name AS customer_name, c.phone AS customer_phone " +
                        "FROM Invoice i " +
                        "JOIN Booking b ON i.booking_id = b.booking_id " +
                        "JOIN Customer c ON b.customer_id = c.id " +
                        "WHERE i.booking_id = ?", bookingId);
        return rows.stream().findFirst();
    }

    // one payment with its customer and booking details, for the receipt page.
    // balance_remaining = what was still owed right after THIS payment
    public Optional<Map<String, Object>> findReceipt(int paymentId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT p.payment_id, CONCAT('RCPT-', LPAD(p.payment_id, 5, '0')) AS receipt_no, " +
                        "p.booking_id, p.amount, p.payment_type, p.payment_status, " +
                        "DATE_FORMAT(p.payment_date, '%Y-%m-%d') AS payment_date, " +
                        EVENT_TYPE_SQL + " AS event_type, DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, " +
                        "c.name AS customer_name, c.phone AS customer_phone, " +
                        "CASE WHEN b.status = 'CANCELLED' THEN 0 " +
                        "ELSE GREATEST(" + TOTAL_PRICE_SQL + " - COALESCE((SELECT SUM(x.amount) FROM Payment x " +
                        "WHERE x.booking_id = b.booking_id AND x.payment_type = 'Balance' AND x.payment_id <= p.payment_id), 0), 0) " +
                        "END AS balance_remaining " +
                        "FROM Payment p " +
                        "JOIN Booking b ON p.booking_id = b.booking_id " +
                        "JOIN Customer c ON b.customer_id = c.id " +
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