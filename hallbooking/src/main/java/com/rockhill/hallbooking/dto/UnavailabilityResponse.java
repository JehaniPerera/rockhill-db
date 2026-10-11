package com.rockhill.hallbooking.dto;

import com.rockhill.hallbooking.entity.StaffAvailability;

import java.time.LocalDate;

public record UnavailabilityResponse(Long availabilityId, Long staffId, LocalDate date, String reason) {

    public static UnavailabilityResponse from(StaffAvailability a) {
        return new UnavailabilityResponse(a.getAvailabilityId(), a.getStaff().getStaffId(),
                a.getUnavailableDate(), a.getReason());
    }
}
