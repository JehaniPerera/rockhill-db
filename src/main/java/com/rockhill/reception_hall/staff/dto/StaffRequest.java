package com.rockhill.reception_hall.staff.dto;

import com.rockhill.reception_hall.staff.entity.StaffType;

public record StaffRequest(
        String name,
        String role,
        StaffType type,
        String contact) {
}
