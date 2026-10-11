package com.rockhill.hallbooking.dto;

import com.rockhill.hallbooking.entity.StaffType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StaffRequest(
        @NotBlank(message = "Name is required") @Size(max = 100) String name,
        @NotBlank(message = "Role is required") @Size(max = 50) String role,
        @NotNull(message = "Type is required (Internal or Vendor)") StaffType type,
        @Size(max = 100) String contact) {
}
