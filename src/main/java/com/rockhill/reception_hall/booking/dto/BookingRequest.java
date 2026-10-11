package com.rockhill.reception_hall.booking.dto;

import com.rockhill.reception_hall.booking.model.EventType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BookingRequest(
        @NotBlank(message = "Name is required") @Size(max = 100) String customerName,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^(\\d{10}|\\+\\d{9,14})$",
                message = "Phone must be 10 digits or + followed by 9-14 digits") String customerPhone,
        @NotBlank(message = "Email is required")
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
                message = "Email must be valid and contain @") @Size(max = 100) String customerEmail,
        @NotNull(message = "Event date is required") @Future LocalDate eventDate,
        @NotNull(message = "Event type is required") EventType eventType,
        @Min(value = 1, message = "Guest count is required") int guestCount,
        @NotNull(message = "Package or menu is required") Long selectionId,
        @Size(max = 50) String eventDescription) {
}