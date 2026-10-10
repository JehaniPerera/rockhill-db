package com.rockhill.hallbooking.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AssignRequest(@NotEmpty(message = "Select at least one staff/vendor") List<Long> staffIds) {
}
