package com.rockhill.hallbooking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DamageReportRequest(
        @NotNull(message = "Select whether the hall is damaged or not") Boolean damaged,
        @Size(max = 500, message = "Notes must be at most 500 characters") String notes) {
}
