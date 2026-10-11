package com.rockhill.hallbooking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UnavailabilityRequest(
        @NotNull(message = "Date is required (yyyy-MM-dd)")
        @FutureOrPresent(message = "Date cannot be in the past") LocalDate date,
        @Size(max = 255) String reason) {
}
