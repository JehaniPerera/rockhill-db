package com.rockhill.reception_hall.payment;

import java.math.BigDecimal;

public record PaymentProof(int proofId, int bookingId, String paymentType, BigDecimal amount,
                           String status, String fileName, String contentType) {
}
