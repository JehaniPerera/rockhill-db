package com.rockhill.reception_hall.payment;

import java.math.BigDecimal;

public record BookingInfo(int bookingId, String status, BigDecimal totalPrice, boolean venueDamaged) {
}
