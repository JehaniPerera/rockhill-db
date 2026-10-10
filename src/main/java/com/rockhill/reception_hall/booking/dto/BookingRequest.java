package com.rockhill.reception_hall.booking.dto;

import com.rockhill.reception_hall.booking.model.EventType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BookingRequest(
        @NotBlank String customerName,
        @NotBlank String customerPhone,
        @Email String customerEmail,
        @NotNull @Future LocalDate eventDate,
        @NotNull EventType eventType,
        @Min(1) int guestCount) {
}