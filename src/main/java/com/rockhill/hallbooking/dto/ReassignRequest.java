package com.rockhill.hallbooking.dto;

import jakarta.validation.constraints.NotNull;

public record ReassignRequest(@NotNull(message = "newStaffId is required") Long newStaffId) {
}
