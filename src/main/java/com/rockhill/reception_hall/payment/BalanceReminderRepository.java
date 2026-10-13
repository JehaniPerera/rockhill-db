package com.rockhill.reception_hall.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class BalanceReminderRepository {

    private final JdbcTemplate jdbc;
    private final PaymentRepository payments;

    public BalanceReminderRepository(JdbcTemplate jdbc, PaymentRepository payments) {
        this.jdbc = jdbc;
        this.payments = payments;
    }

    // Confirmed bookings (deposit paid) whose call-reminder date has arrived: today >= event date - 17 days.
    // Nothing is stored: the reminder is worked out from the event date every time, so a booking can never
    // have a duplicate reminder, and it disappears from the active list once the approved balance
    // installments add up to the price.
    public List<Map<String, Object>> findReminders(boolean includeCompleted) {
        // total of the approved Balance installments of this booking
        String paid = "COALESCE((SELECT SUM(y.amount) FROM Payment y WHERE y.booking_id = b.booking_id AND y.payment_type = 'Balance'), 0)";
        String sql =
                "SELECT b.booking_id AS booking_id, c.name AS customer_name, c.phone AS customer_phone, " + PaymentRepository.TOTAL_PRICE_SQL + " AS total_price, " +
                        "DATE_FORMAT(b.event_date, '%Y-%m-%d') AS event_date, " +
                        "DATE_FORMAT(DATE_SUB(b.event_date, INTERVAL 17 DAY), '%Y-%m-%d') AS reminder_date, " +
                        "DATE_FORMAT(DATE_SUB(b.event_date, INTERVAL 14 DAY), '%Y-%m-%d') AS balance_deadline, " +
                        "DATEDIFF(DATE_SUB(b.event_date, INTERVAL 14 DAY), CURDATE()) AS days_left, " +
                        "(" + paid + " >= " + PaymentRepository.TOTAL_PRICE_SQL + ") AS balance_paid, " +
                        paid + " AS balance_paid_amount " +
                        "FROM Booking b JOIN Customer c ON b.customer_id = c.id " +
                        "WHERE b.status IN ('CONFIRMED','COMPLETED') " +
                        "AND CURDATE() >= DATE_SUB(b.event_date, INTERVAL 17 DAY) " +
                        "AND EXISTS (SELECT 1 FROM Payment d WHERE d.booking_id = b.booking_id AND d.payment_type = 'Deposit') " +
                        (includeCompleted
                                // paid bookings are only listed until their event has passed
                                ? "AND (b.event_date >= CURDATE() OR " + paid + " < " + PaymentRepository.TOTAL_PRICE_SQL + ") "
                                : "AND " + paid + " < " + PaymentRepository.TOTAL_PRICE_SQL + " ") +
                        "ORDER BY b.event_date, b.booking_id";
        return jdbc.queryForList(sql);
    }

}