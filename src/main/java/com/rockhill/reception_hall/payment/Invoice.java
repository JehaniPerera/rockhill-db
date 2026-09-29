package com.rockhill.reception_hall.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Invoice {

    private int invoiceId;
    private int bookingId;
    private BigDecimal totalAmount;
    private LocalDate issueDate;

    public Invoice() {
    }

    public Invoice(int invoiceId, int bookingId, BigDecimal totalAmount, LocalDate issueDate) {
        this.invoiceId = invoiceId;
        this.bookingId = bookingId;
        this.totalAmount = totalAmount;
        this.issueDate = issueDate;
    }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
}