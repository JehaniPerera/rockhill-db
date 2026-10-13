package com.rockhill.reception_hall.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository payments;

    @Mock
    private InvoiceRepository invoices;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(payments, invoices);
    }

    // ---------- helpers ----------

    private void givenBooking(String status, String price, boolean damaged) {
        when(payments.findBooking(1))
                .thenReturn(Optional.of(new BookingInfo(1, status, new BigDecimal(price), damaged)));
        lenient().when(payments.isAvailabilityApproved(1)).thenReturn(true);
    }

    private void givenPaymentCount(Payment.PaymentType type, int count) {
        when(payments.countPayments(1, type)).thenReturn(count);
    }

    private void saveReturnsArgument() {
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------- Deposit ----------

    @Test
    void deposit_pendingBooking_isSavedAndBookingConfirmed() {
        givenBooking("Pending", "590000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 0);
        saveReturnsArgument();

        Payment result = service.recordDeposit(1);

        assertEquals(0, new BigDecimal("20000.00").compareTo(result.getAmount()));
        assertEquals(Payment.PaymentType.DEPOSIT, result.getPaymentType());
        verify(payments).confirmBooking(1);
    }

    @Test
    void deposit_cancelledBooking_isRejected() {
        givenBooking("Cancelled", "263000.00", false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.recordDeposit(1));

        assertTrue(ex.getMessage().contains("Cancelled"));
        verify(payments, never()).save(any());
    }

    @Test
    void deposit_alreadyPaid_isRejected() {
        givenBooking("Confirmed", "815000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 1);

        assertThrows(IllegalStateException.class, () -> service.recordDeposit(1));
        verify(payments, never()).save(any());
    }

    // ---------- Balance ----------

    @Test
    void balance_withoutDeposit_isRejected() {
        givenBooking("Confirmed", "815000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 0);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.recordBalance(1));

        assertEquals("Pay the deposit first", ex.getMessage());
    }

    @Test
    void balance_afterDeposit_equalsFullTotalPrice() {
        givenBooking("Confirmed", "590000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 1);
        givenPaymentCount(Payment.PaymentType.BALANCE, 0);
        saveReturnsArgument();

        Payment result = service.recordBalance(1);

        // the Rs. 20,000 is a separate refundable deposit, so balance = full total
        assertEquals(0, new BigDecimal("590000.00").compareTo(result.getAmount()));
        assertEquals(Payment.PaymentStatus.FULLY_PAID, result.getPaymentStatus());
    }

    @Test
    void balance_alreadyPaid_isRejected() {
        givenBooking("Confirmed", "590000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 1);
        givenPaymentCount(Payment.PaymentType.BALANCE, 1);

        assertThrows(IllegalStateException.class, () -> service.recordBalance(1));
        verify(payments, never()).save(any());
    }

    @Test
    void balance_onPendingBooking_isRejected() {
        givenBooking("Pending", "590000.00", false);

        assertThrows(IllegalStateException.class, () -> service.recordBalance(1));
    }

    // ---------- Invoice ----------

    @Test
    void invoice_cancelledBooking_isRejected() {
        givenBooking("Cancelled", "263000.00", false);

        assertThrows(IllegalStateException.class, () -> service.generateInvoice(1));
        verify(invoices, never()).save(any());
    }

    @Test
    void invoice_beforeBalancePaid_isRejected() {
        givenBooking("Confirmed", "815000.00", false);
        givenPaymentCount(Payment.PaymentType.BALANCE, 0);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.generateInvoice(1));

        assertTrue(ex.getMessage().contains("Balance must be paid"));
    }

    @Test
    void invoice_afterBalancePaid_isCreatedWithBookingTotal() {
        givenBooking("Confirmed", "815000.00", false);
        givenPaymentCount(Payment.PaymentType.BALANCE, 1);
        when(invoices.findByBookingId(1)).thenReturn(Optional.empty());
        when(invoices.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = service.generateInvoice(1);

        assertEquals(0, new BigDecimal("815000.00").compareTo(result.getTotalAmount()));
        assertEquals(1, result.getBookingId());
        assertNotNull(result.getIssueDate());
    }

    @Test
    void invoice_alreadyExists_isReturnedWithoutCreatingSecondOne() {
        givenBooking("Confirmed", "815000.00", false);
        givenPaymentCount(Payment.PaymentType.BALANCE, 1);
        Invoice existing = new Invoice(5, 1, new BigDecimal("815000.00"), LocalDate.now());
        when(invoices.findByBookingId(1)).thenReturn(Optional.of(existing));

        Invoice result = service.generateInvoice(1);

        assertSame(existing, result);
        verify(invoices, never()).save(any());
    }

    // ---------- Refund ----------

    @Test
    void refund_cancelledBooking_depositForfeited() {
        givenBooking("Cancelled", "263000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 1);

        String message = service.processRefund(1);

        verify(payments).updateDepositRefundStatus(1, Payment.RefundStatus.FORFEITED);
        assertTrue(message.contains("forfeited"));
    }

    @Test
    void refund_completedWithDamage_depositForfeited() {
        givenBooking("Completed", "465400.00", true);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 1);

        service.processRefund(1);

        verify(payments).updateDepositRefundStatus(1, Payment.RefundStatus.FORFEITED);
    }

    @Test
    void refund_completedNoDamage_depositRefunded() {
        givenBooking("Completed", "590000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 1);

        service.processRefund(1);

        verify(payments).updateDepositRefundStatus(1, Payment.RefundStatus.REFUNDED);
    }

    @Test
    void refund_confirmedBooking_isRejected() {
        givenBooking("Confirmed", "815000.00", false);

        assertThrows(IllegalStateException.class, () -> service.processRefund(1));
        verify(payments, never()).updateDepositRefundStatus(anyInt(), any());
    }

    @Test
    void anyAction_unknownBooking_isRejected() {
        when(payments.findBooking(99)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.recordDeposit(99));

        assertEquals("Booking not found", ex.getMessage());
    }

    // ---------- Late booking (event less than 14 days away) ----------

    @Test
    void deposit_lateBooking_isRejected() {
        givenBooking("Pending", "590000.00", false);
        when(payments.isWithinBalanceWindow(1)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.recordDeposit(1));

        assertTrue(ex.getMessage().contains("full amount must be paid at once"));
        verify(payments, never()).save(any());
    }

    @Test
    void fullPayment_savesDepositAndBalance_andConfirmsBooking() {
        givenBooking("Pending", "590000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 0);
        saveReturnsArgument();

        service.recordFullPayment(1);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(payments, times(2)).save(captor.capture());
        assertEquals(Payment.PaymentType.DEPOSIT, captor.getAllValues().get(0).getPaymentType());
        assertEquals(Payment.PaymentType.BALANCE, captor.getAllValues().get(1).getPaymentType());
        assertEquals(0, new BigDecimal("590000.00").compareTo(captor.getAllValues().get(1).getAmount()));
        verify(payments).confirmBooking(1);
    }

    @Test
    void fullPayment_whenDepositAlreadyPaid_isRejected() {
        givenBooking("Confirmed", "590000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 1);

        assertThrows(IllegalStateException.class, () -> service.recordFullPayment(1));
        verify(payments, never()).save(any());
    }

    // ---------- Admin approval and duplicate refund ----------

    @Test
    void deposit_dateNotApprovedByAdmin_isRejected() {
        givenBooking("Pending", "453000.00", false);
        doReturn(false).when(payments).isAvailabilityApproved(1);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.recordDeposit(1));

        assertTrue(ex.getMessage().contains("not approved"));
        verify(payments, never()).save(any());
    }

    @Test
    void refund_alreadyProcessed_isRejected() {
        givenBooking("Completed", "590000.00", false);
        givenPaymentCount(Payment.PaymentType.DEPOSIT, 1);
        when(payments.isRefundProcessed(1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.processRefund(1));
        verify(payments, never()).updateDepositRefundStatus(anyInt(), any());
    }
}
