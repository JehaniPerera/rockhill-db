package com.rockhill.reception_hall.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PaymentProofRepository {

    private final JdbcTemplate jdbc;

    public PaymentProofRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int insert(int bookingId, String paymentType, BigDecimal amount, String bankReference,
                      String fileName, String contentType) {
        String sql = "INSERT INTO PaymentProof (booking_id, payment_type, amount, bank_reference, file_name, content_type) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bookingId);
            ps.setString(2, paymentType);
            ps.setBigDecimal(3, amount);
            ps.setString(4, bankReference);
            ps.setString(5, fileName);
            ps.setString(6, contentType);
            return ps;
        }, keys);
        return keys.getKey().intValue();
    }

    public Optional<PaymentProof> findById(int proofId) {
        List<PaymentProof> rows = jdbc.query(
                "SELECT proof_id, booking_id, payment_type, amount, status, file_name, content_type FROM PaymentProof WHERE proof_id = ?",
                (rs, i) -> new PaymentProof(rs.getInt("proof_id"), rs.getInt("booking_id"), rs.getString("payment_type"),
                        rs.getBigDecimal("amount"), rs.getString("status"), rs.getString("file_name"),
                        rs.getString("content_type")),
                proofId);
        return rows.stream().findFirst();
    }

    public int countPending(int bookingId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM PaymentProof WHERE booking_id = ? AND status = 'Pending Approval'",
                Integer.class, bookingId);
        return n == null ? 0 : n;
    }

    // returns how many rows changed (0 means somebody already reviewed it)
    public int markApproved(int proofId) {
        return jdbc.update(
                "UPDATE PaymentProof SET status = 'Approved', reviewed_at = NOW() WHERE proof_id = ? AND status = 'Pending Approval'",
                proofId);
    }

    public int markRejected(int proofId, String reason) {
        return jdbc.update(
                "UPDATE PaymentProof SET status = 'Rejected', rejection_reason = ?, reviewed_at = NOW() WHERE proof_id = ? AND status = 'Pending Approval'",
                reason, proofId);
    }

    public List<Map<String, Object>> findByBooking(int bookingId) {
        return jdbc.queryForList(
                "SELECT proof_id, booking_id, payment_type, amount, bank_reference, status, rejection_reason, " +
                        "DATE_FORMAT(submitted_at, '%Y-%m-%d %H:%i') AS submitted_at, " +
                        "DATE_FORMAT(reviewed_at, '%Y-%m-%d %H:%i') AS reviewed_at " +
                        "FROM PaymentProof WHERE booking_id = ? ORDER BY proof_id DESC", bookingId);
    }

    public List<Map<String, Object>> findPending() {
        return jdbc.queryForList(
                "SELECT pp.proof_id, pp.booking_id, c.name AS customer_name, pp.payment_type, pp.amount, pp.bank_reference, " +
                        "DATE_FORMAT(pp.submitted_at, '%Y-%m-%d %H:%i') AS submitted_at " +
                        "FROM PaymentProof pp " +
                        "JOIN Booking b ON pp.booking_id = b.id " +
                        "JOIN Customer c ON b.customer_id = c.id " +
                        "WHERE pp.status = 'Pending Approval' ORDER BY pp.proof_id");
    }
}