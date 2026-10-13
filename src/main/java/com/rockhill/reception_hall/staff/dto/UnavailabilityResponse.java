package com.rockhill.reception_hall.staff.dto;

import com.rockhill.reception_hall.staff.entity.StaffAvailability;

import java.time.LocalDate;

public record UnavailabilityResponse(Long availabilityId, Long staffId, LocalDate date, String reason) {

    public static UnavailabilityResponse from(StaffAvailability a) {
        return new UnavailabilityResponse(a.getAvailabilityId(), a.getStaff().getStaffId(),
                a.getUnavailableDate(), a.getReason());
    }
}
