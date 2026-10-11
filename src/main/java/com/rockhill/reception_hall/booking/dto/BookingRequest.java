package com.rockhill.reception_hall.booking.dto;

import com.rockhill.reception_hall.booking.model.EventType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BookingRequest(
        @NotBlank @Size(max = 100) String customerName,
        @NotBlank @Size(max = 15) String customerPhone,
        @Email @Size(max = 100) String customerEmail,
        @NotNull @Future LocalDate eventDate,
        @NotNull EventType eventType,
        @Min(1) int guestCount,
        @NotNull Long selectionId,                 // package id (wedding) or menu id (other event)
        @Size(max = 50) String eventDescription) { // other events only, e.g. "Birthday"
}