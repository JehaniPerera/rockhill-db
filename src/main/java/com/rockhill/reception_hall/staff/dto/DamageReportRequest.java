package com.rockhill.reception_hall.staff.dto;


public record DamageReportRequest(
        Boolean damaged,
        String notes) {
}
