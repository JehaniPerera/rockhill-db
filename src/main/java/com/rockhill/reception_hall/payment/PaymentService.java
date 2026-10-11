package com.rockhill.reception_hall.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("20000.00");

    // false = the Rs. 20,000 is a separate refundable deposit, paid in addition to the full price
    //         (balance = full total; cancellation or venue damage forfeits the deposit)
    // true  = the deposit counts toward the price (balance = total - deposit)
    static final boolean DEPOSIT_COUNTS_TOWARD_PRICE = false;

    private final PaymentRepository payments;
    private final InvoiceRepository invoices;

    public PaymentService(PaymentRepository payments, InvoiceRepository invoices) {
        this.payments = payments;
        this.invoices = invoices;
    }

    @Transactional
    public Payment recordDeposit(int bookingId) {
        BookingInfo booking = requireBooking(bookingId);
        if (!"Pending".equals(booking.status()) && !"Confirmed".equals(booking.status())) {
            throw new IllegalStateException("Deposit cannot be paid for a " + booking.status() + " booking");
        }
        requireApproved(booking);
        if (payments.isWithinBalanceWindow(bookingId)) {
            throw new IllegalStateException("The event is less than 14 days away: the full amount must be paid at once");
        }
        if (payments.countPayments(bookingId, Payment.PaymentType.DEPOSIT) > 0) {
            throw new IllegalStateException("Deposit already paid for this booking");
        }
        Payment p = payments.save(newPayment(bookingId, DEPOSIT_AMOUNT,
                Payment.PaymentType.DEPOSIT, Payment.PaymentStatus.PARTIALLY_PAID));
        payments.confirmBooking(bookingId);
        return p;
    }

    public Payment recordBalance(int bookingId) {
        BookingInfo booking = requireBooking(bookingId);
        if (!"Confirmed".equals(booking.status())) {
            throw new IllegalStateException("Balance can only be paid for a Confirmed booking (current status: " + booking.status() + ")");
        }
        if (payments.countPayments(bookingId, Payment.PaymentType.DEPOSIT) == 0) {
            throw new IllegalStateException("Pay the deposit first");
        }
        if (payments.countPayments(bookingId, Payment.PaymentType.BALANCE) > 0) {
            throw new IllegalStateException("Balance already paid for this booking");
        }
        return payments.save(newPayment(bookingId, balanceAmount(booking),
                Payment.PaymentType.BALANCE, Payment.PaymentStatus.FULLY_PAID));
    }

    // late booking rule: deposit and full balance are paid together, in one step
    @Transactional
    public String recordFullPayment(int bookingId) {
        BookingInfo booking = requireBooking(bookingId);
        if (!"Pending".equals(booking.status()) && !"Confirmed".equals(booking.status())) {
            throw new IllegalStateException("Payment cannot be made for a " + booking.status() + " booking");
        }
        requireApproved(booking);
        if (payments.countPayments(bookingId, Payment.PaymentType.DEPOSIT) > 0) {
            throw new IllegalStateException("Deposit already paid for this booking");
        }
        payments.save(newPayment(bookingId, DEPOSIT_AMOUNT,
                Payment.PaymentType.DEPOSIT, Payment.PaymentStatus.PARTIALLY_PAID));
        payments.confirmBooking(bookingId);
        payments.save(newPayment(bookingId, balanceAmount(booking),
                Payment.PaymentType.BALANCE, Payment.PaymentStatus.FULLY_PAID));
        return "Full amount paid. Your booking is confirmed.";
    }

    public Invoice generateInvoice(int bookingId) {
        BookingInfo booking = requireBooking(bookingId);
        if ("Cancelled".equals(booking.status())) {
            throw new IllegalStateException("No invoice can be issued for a Cancelled booking");
        }
        if (payments.countPayments(bookingId, Payment.PaymentType.BALANCE) == 0) {
            throw new IllegalStateException("Balance must be paid before an invoice is issued");
        }
        return invoices.findByBookingId(bookingId).orElseGet(() ->
                invoices.save(new Invoice(0, bookingId, booking.totalPrice(), LocalDate.now())));
    }

    public Map<String, Object> getInvoiceDetails(int bookingId) {
        return payments.findInvoiceDetails(bookingId)
                .orElseThrow(() -> new IllegalStateException("No invoice has been generated for this booking yet"));
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

    // a Pending booking can only be paid once the administrator has approved the date
    private void requireApproved(BookingInfo booking) {
        if ("Pending".equals(booking.status()) && !payments.isAvailabilityApproved(booking.bookingId())) {
            throw new IllegalStateException("The administrator has not approved this date yet. Please wait for approval before paying.");
        }
    }

    private BigDecimal balanceAmount(BookingInfo booking) {
        return DEPOSIT_COUNTS_TOWARD_PRICE
                ? booking.totalPrice().subtract(DEPOSIT_AMOUNT)
                : booking.totalPrice();
    }

    private BookingInfo requireBooking(int bookingId) {
        return payments.findBooking(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));
    }

    private Payment newPayment(int bookingId, BigDecimal amount,
                               Payment.PaymentType type, Payment.PaymentStatus status) {
        return new Payment(0, bookingId, amount, type, LocalDate.now(), status, Payment.RefundStatus.NA);
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