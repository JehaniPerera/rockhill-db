package com.rockhill.reception_hall.staff.dto;


import java.time.LocalDate;

public record UnavailabilityRequest(
        LocalDate date,
        String reason) {
}
