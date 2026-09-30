package com.rockhill.reception_hall.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {

    public enum PaymentType {
        DEPOSIT("Deposit"), BALANCE("Balance");
        private final String dbValue;
        PaymentType(String dbValue) { this.dbValue = dbValue; }
        public String dbValue() { return dbValue; }
    }

    public enum PaymentStatus {
        PENDING("Pending"), PARTIALLY_PAID("Partially Paid"), FULLY_PAID("Fully Paid");
        private final String dbValue;
        PaymentStatus(String dbValue) { this.dbValue = dbValue; }
        public String dbValue() { return dbValue; }
    }

    public enum RefundStatus {
        NA("N/A"), FORFEITED("Forfeited"), REFUND_DUE("Refund Due"), REFUNDED("Refunded");
        private final String dbValue;
        RefundStatus(String dbValue) { this.dbValue = dbValue; }
        public String dbValue() { return dbValue; }
    }

    private int paymentId;
    private int bookingId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private LocalDate paymentDate;
    private PaymentStatus paymentStatus;
    private RefundStatus refundStatus;

    public Payment() {
    }

    public Payment(int paymentId, int bookingId, BigDecimal amount, PaymentType paymentType,
                   LocalDate paymentDate, PaymentStatus paymentStatus, RefundStatus refundStatus) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentType = paymentType;
        this.paymentDate = paymentDate;
        this.paymentStatus = paymentStatus;
        this.refundStatus = refundStatus;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public RefundStatus getRefundStatus() { return refundStatus; }
    public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }
}