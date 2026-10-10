package com.rockhill.reception_hall.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class InvoiceRepository {

    private final JdbcTemplate jdbc;

    public InvoiceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Invoice> findByBookingId(int bookingId) {
        List<Invoice> rows = jdbc.query(
                "SELECT invoice_id, booking_id, total_amount, issue_date FROM Invoice WHERE booking_id = ?",
                (rs, i) -> new Invoice(rs.getInt("invoice_id"), rs.getInt("booking_id"),
                        rs.getBigDecimal("total_amount"), rs.getDate("issue_date").toLocalDate()),
                bookingId);
        return rows.stream().findFirst();
    }

    public Invoice save(Invoice invoice) {
        String sql = "INSERT INTO Invoice (booking_id, total_amount, issue_date) VALUES (?, ?, ?)";
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, invoice.getBookingId());
            ps.setBigDecimal(2, invoice.getTotalAmount());
            ps.setDate(3, Date.valueOf(invoice.getIssueDate()));
            return ps;
        }, keys);
        invoice.setInvoiceId(keys.getKey().intValue());
        return invoice;
    }
}