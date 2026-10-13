package com.rockhill.hallbooking.dto;

import com.rockhill.hallbooking.entity.HallDamageReport;

import java.time.LocalDateTime;

public record DamageReportResponse(Long reportId, Long bookingId, boolean damaged, String notes,
                                   LocalDateTime reportedAt) {

    public static DamageReportResponse from(HallDamageReport r) {
        return new DamageReportResponse(r.getReportId(), r.getBookingId(), r.isDamaged(),
                r.getNotes(), r.getReportedAt());
    }
}
