package com.rockhill.reception_hall.staff.dto;

import com.rockhill.reception_hall.staff.entity.HallDamageReport;

import java.time.LocalDateTime;

public record DamageReportResponse(Long reportId, Long bookingId, boolean damaged, String notes,
                                   LocalDateTime reportedAt) {

    public static DamageReportResponse from(HallDamageReport r) {
        return new DamageReportResponse(r.getReportId(), r.getBookingId(), r.isDamaged(),
                r.getNotes(), r.getReportedAt());
    }
}
