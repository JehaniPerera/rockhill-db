package com.rockhill.reception_hall.payment;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BalanceReminderService {

    static final String PENDING = "Pending";
    static final String COMPLETED = "Completed";
    static final String OVERDUE = "Overdue";

    private final BalanceReminderRepository reminders;

    public BalanceReminderService(BalanceReminderRepository reminders) {
        this.reminders = reminders;
    }

    public List<Map<String, Object>> getReminders(boolean includeCompleted) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : reminders.findReminders(includeCompleted)) {
            boolean paid = ((Number) row.get("balance_paid")).intValue() > 0;
            int daysLeft = ((Number) row.get("days_left")).intValue();
            BigDecimal total = (BigDecimal) row.get("total_price");
            BigDecimal paidSoFar = new BigDecimal(String.valueOf(row.get("balance_paid_amount")));

            // same rule as PaymentService: the balance is the full price unless the deposit counts toward it,
            // minus the installments already approved
            BigDecimal balance = PaymentService.DEPOSIT_COUNTS_TOWARD_PRICE
                    ? total.subtract(PaymentService.DEPOSIT_AMOUNT) : total;
            BigDecimal outstanding = paid ? BigDecimal.ZERO : balance.subtract(paidSoFar).max(BigDecimal.ZERO);

            // overdue uses the same rule as the existing booking list: the day after the deadline
            String status = paid ? COMPLETED : (daysLeft < 0 ? OVERDUE : PENDING);

            Map<String, Object> r = new LinkedHashMap<>();
            r.put("booking_id", row.get("booking_id"));
            r.put("customer_name", row.get("customer_name"));
            r.put("customer_phone", row.get("customer_phone"));
            r.put("event_date", row.get("event_date"));
            r.put("reminder_date", row.get("reminder_date"));
            r.put("balance_deadline", row.get("balance_deadline"));
            r.put("outstanding_balance", outstanding);
            r.put("days_left", daysLeft);
            r.put("status", status);
            r.put("message", messageFor(status, daysLeft));
            result.add(r);
        }
        return result;
    }

    private String messageFor(String status, int daysLeft) {
        if (COMPLETED.equals(status)) {
            return "Balance paid. No call needed.";
        }
        if (OVERDUE.equals(status)) {
            return "The balance deadline has passed. The deposit forfeiture rule now applies: "
                    + "check for a payment proof waiting for approval, then cancel the booking on the booking page and process the deposit outcome here.";
        }
        if (daysLeft == 0) {
            return "Call this customer! Today is the last day to pay the balance. "
                    + "Their deposit will be forfeited if the balance payment is not completed.";
        }
        return "Call this customer! Their deposit will be forfeited in " + daysLeft
                + (daysLeft == 1 ? " day" : " days") + " if the balance payment is not completed.";
    }
}
