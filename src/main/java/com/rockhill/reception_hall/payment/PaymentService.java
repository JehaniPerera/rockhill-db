package com.rockhill.reception_hall.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PaymentService {

    static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("20000.00");

    // smallest balance installment a customer may pay (the LAST installment may be smaller)
    static final BigDecimal MIN_INSTALLMENT = new BigDecimal("10000.00");

    // false = the Rs. 20,000 is a separate refundable deposit, paid in addition to the full price
    //         (balance = full total; cancellation or venue damage forfeits the deposit)
    // true  = the deposit counts toward the price (balance = total - deposit)
    // note: the SQL queries in PaymentRepository assume false
    static final boolean DEPOSIT_COUNTS_TOWARD_PRICE = false;

    private final PaymentRepository payments;
    private final InvoiceRepository invoices;

    public PaymentService(PaymentRepository payments, InvoiceRepository invoices) {
        this.payments = payments;
        this.invoices = invoices;
    }

    // ---- checks that save nothing: used when a customer uploads a payment proof ----

    public void validateDeposit(int bookingId) {
        checkDeposit(requireBooking(bookingId), true);
    }

    // returns the amount this installment will be recorded as (null request = everything that is left)
    public BigDecimal validateBalance(int bookingId, BigDecimal requested) {
        BookingInfo booking = requireBooking(bookingId);
        BigDecimal remaining = checkBalance(booking);
        return chooseInstallment(bookingId, remaining, requested);
    }

    public void validateFull(int bookingId) {
        BookingInfo booking = requireBooking(bookingId);
        checkFull(booking);
        // the combined payment is only for late bookings; otherwise pay the deposit first, then the balance
        if (!payments.isWithinBalanceWindow(booking.bookingId())) {
            throw new IllegalStateException("The full amount at once is only for events less than 14 days away. Please pay the deposit first.");
        }
    }

    public BigDecimal depositAmount() {
        return DEPOSIT_AMOUNT;
    }

    // what the customer still owes on the balance (price minus the approved installments)
    public BigDecimal balanceRemainingFor(int bookingId) {
        return balanceRemaining(requireBooking(bookingId));
    }

    public BigDecimal fullAmountFor(int bookingId) {
        return DEPOSIT_AMOUNT.add(balanceAmount(requireBooking(bookingId)));
    }

    // ---- recording a payment (called when the administrator approves a payment proof) ----

    @Transactional
    public Payment recordDeposit(int bookingId) {
        // the 14-day rule was checked when the receipt was uploaded: time passing while the receipt
        // waits for approval must not stop the administrator from approving a valid deposit
        checkDeposit(requireBooking(bookingId), false);
        Payment p = payments.save(newPayment(bookingId, DEPOSIT_AMOUNT,
                Payment.PaymentType.DEPOSIT, Payment.PaymentStatus.PARTIALLY_PAID));
        payments.confirmBooking(bookingId);
        return p;
    }

    // one balance installment. The installment that brings the total to the price is marked Fully Paid.
    // (the minimum and due-date rules were already checked when the receipt was uploaded)
    @Transactional
    public Payment recordBalance(int bookingId, BigDecimal requested) {
        BookingInfo booking = requireBooking(bookingId);
        BigDecimal remaining = checkBalance(booking);
        BigDecimal amount = requested == null ? remaining : requested;
        if (amount.signum() <= 0 || amount.compareTo(remaining) > 0) {
            throw new IllegalStateException("The payment amount is not valid for the remaining balance (" + money(remaining) + ").");
        }
        boolean last = amount.compareTo(remaining) == 0;
        return payments.save(newPayment(bookingId, amount, Payment.PaymentType.BALANCE,
                last ? Payment.PaymentStatus.FULLY_PAID : Payment.PaymentStatus.PARTIALLY_PAID));
    }

    // pays everything that is left in one go
    public Payment recordBalance(int bookingId) {
        return recordBalance(bookingId, null);
    }

    // late booking rule: deposit and full balance are paid together, in one step
    @Transactional
    public String recordFullPayment(int bookingId) {
        BookingInfo booking = requireBooking(bookingId);
        checkFull(booking);
        payments.save(newPayment(bookingId, DEPOSIT_AMOUNT,
                Payment.PaymentType.DEPOSIT, Payment.PaymentStatus.PARTIALLY_PAID));
        payments.confirmBooking(bookingId);
        payments.save(newPayment(bookingId, balanceAmount(booking),
                Payment.PaymentType.BALANCE, Payment.PaymentStatus.FULLY_PAID));
        return "Full amount paid. Your booking is confirmed.";
    }

    // ---- invoice and refund ----

    public Invoice generateInvoice(int bookingId) {
        BookingInfo booking = requireBooking(bookingId);
        if ("Cancelled".equals(booking.status())) {
            throw new IllegalStateException("No invoice can be issued for a Cancelled booking");
        }
        if (balanceRemaining(booking).signum() > 0) {
            throw new IllegalStateException("Balance must be paid in full before an invoice is issued");
        }
        return invoices.findByBookingId(bookingId).orElseGet(() ->
                invoices.save(new Invoice(0, bookingId, booking.totalPrice(), LocalDate.now())));
    }

    public Map<String, Object> getInvoiceDetails(int bookingId) {
        return payments.findInvoiceDetails(bookingId)
                .orElseThrow(() -> new IllegalStateException("No invoice has been generated for this booking yet"));
    }

    public Map<String, Object> getReceipt(int paymentId) {
        return payments.findReceipt(paymentId)
                .orElseThrow(() -> new IllegalStateException("Receipt not found"));
    }

    public String processRefund(int bookingId) {
        BookingInfo booking = requireBooking(bookingId);
        String status = booking.status();
        if (!"Cancelled".equals(status) && !"Completed".equals(status)) {
            throw new IllegalStateException("Refund can only be processed for a Cancelled or Completed booking (current status: " + status + ")");
        }
        if (payments.countPayments(bookingId, Payment.PaymentType.DEPOSIT) == 0) {
            throw new IllegalStateException("No deposit payment found for this booking");
        }
        if (payments.isRefundProcessed(bookingId)) {
            throw new IllegalStateException("The deposit outcome has already been recorded for this booking");
        }

        Payment.RefundStatus result;
        String message;
        if ("Cancelled".equals(status)) {
            result = Payment.RefundStatus.FORFEITED;
            message = "Booking was cancelled: deposit forfeited.";
        } else if (DEPOSIT_COUNTS_TOWARD_PRICE) {
            result = Payment.RefundStatus.NA;
            message = "Deposit was part of the price: nothing to refund.";
        } else if (booking.venueDamaged()) {
            result = Payment.RefundStatus.FORFEITED;
            message = "Venue was damaged: deposit forfeited.";
        } else {
            result = Payment.RefundStatus.REFUNDED;
            message = "No damage: deposit refunded.";
        }
        payments.updateDepositRefundStatus(bookingId, result);
        return message;
    }

    // ---- private rules ----

    // checkWindow = true when the customer uploads the receipt; false when the administrator approves it
    private void checkDeposit(BookingInfo booking, boolean checkWindow) {
        int bookingId = booking.bookingId();
        if (!isPayable(booking.status())) {
            throw new IllegalStateException("Deposit cannot be paid for a " + booking.status() + " booking");
        }
        requireApproved(booking);
        if (checkWindow && payments.isWithinBalanceWindow(bookingId)) {
            throw new IllegalStateException("The event is less than 14 days away: the full amount must be paid at once");
        }
        if (payments.countPayments(bookingId, Payment.PaymentType.DEPOSIT) > 0) {
            throw new IllegalStateException("Deposit already paid for this booking");
        }
    }

    // returns how much of the balance is still unpaid
    private BigDecimal checkBalance(BookingInfo booking) {
        int bookingId = booking.bookingId();
        if (!isPayable(booking.status()) || "Pending".equalsIgnoreCase(booking.status())) {
            throw new IllegalStateException("Balance can only be paid for a Confirmed booking (current status: " + booking.status() + ")");
        }
        if (payments.countPayments(bookingId, Payment.PaymentType.DEPOSIT) == 0) {
            throw new IllegalStateException("Pay the deposit first");
        }
        BigDecimal remaining = balanceRemaining(booking);
        if (remaining.signum() == 0) {
            throw new IllegalStateException("Balance already paid for this booking");
        }
        return remaining;
    }

    // the installment rules, checked when the customer uploads a receipt
    private BigDecimal chooseInstallment(int bookingId, BigDecimal remaining, BigDecimal requested) {
        if (requested == null) {
            return remaining;                                   // default: pay everything that is left
        }
        if (requested.scale() > 2) {
            throw new IllegalStateException("The amount can have at most two decimal places.");
        }
        if (requested.signum() <= 0) {
            throw new IllegalStateException("The amount must be more than zero.");
        }
        if (requested.compareTo(remaining) > 0) {
            throw new IllegalStateException("The amount is more than the remaining balance (" + money(remaining) + ").");
        }
        if (requested.compareTo(remaining) < 0) {               // a partial payment (installment)
            if (payments.isWithinBalanceWindow(bookingId)) {
                throw new IllegalStateException("The balance is past its due date, so the full remaining amount ("
                        + money(remaining) + ") must be paid at once.");
            }
            if (requested.compareTo(MIN_INSTALLMENT) < 0) {
                throw new IllegalStateException("The smallest installment is " + money(MIN_INSTALLMENT)
                        + " (unless you pay everything that is left).");
            }
        }
        return requested.setScale(2);
    }

    private void checkFull(BookingInfo booking) {
        if (!isPayable(booking.status())) {
            throw new IllegalStateException("Payment cannot be made for a " + booking.status() + " booking");
        }
        requireApproved(booking);
        if (payments.countPayments(booking.bookingId(), Payment.PaymentType.DEPOSIT) > 0) {
            throw new IllegalStateException("Deposit already paid for this booking");
        }
    }

    // Pending, Confirmed and Completed bookings can still receive payments.
    // "Completed" is what the booking module sets once the hall administrator has confirmed the booking,
    // so a Completed booking that is not fully paid yet must still be payable.
    private static boolean isPayable(String status) {
        return "Pending".equalsIgnoreCase(status)
                || "Confirmed".equalsIgnoreCase(status)
                || "Completed".equalsIgnoreCase(status);
    }

    // a Pending booking can only be paid once the administrator has approved the date
    private void requireApproved(BookingInfo booking) {
        if ("Pending".equals(booking.status()) && !payments.isAvailabilityApproved(booking.bookingId())) {
            throw new IllegalStateException("The administrator has not approved this date yet. Please wait for approval before paying.");
        }
    }

    // the whole balance (before any installment)
    private BigDecimal balanceAmount(BookingInfo booking) {
        return DEPOSIT_COUNTS_TOWARD_PRICE
                ? booking.totalPrice().subtract(DEPOSIT_AMOUNT)
                : booking.totalPrice();
    }

    // the balance minus the installments paid so far
    private BigDecimal balanceRemaining(BookingInfo booking) {
        BigDecimal paid = payments.sumPayments(booking.bookingId(), Payment.PaymentType.BALANCE);
        BigDecimal left = balanceAmount(booking).subtract(paid);
        return left.signum() < 0 ? BigDecimal.ZERO : left;
    }

    private BookingInfo requireBooking(int bookingId) {
        return payments.findBooking(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));
    }

    private Payment newPayment(int bookingId, BigDecimal amount,
                               Payment.PaymentType type, Payment.PaymentStatus status) {
        return new Payment(0, bookingId, amount, type, LocalDate.now(), status, Payment.RefundStatus.NA);
    }

    private static String money(BigDecimal amount) {
        return String.format(Locale.US, "Rs. %,.2f", amount);
    }

    public List<Map<String, Object>> getBookings() {
        return payments.findBookingSummaries();
    }

    public List<Map<String, Object>> getPaymentHistory(int bookingId) {
        return payments.findHistory(bookingId);
    }

    public List<Map<String, Object>> getAllPayments() {
        return payments.findAll();
    }
}