package com.rockhill.reception_hall.staff.dto;

import com.rockhill.reception_hall.staff.entity.Staff;
import com.rockhill.reception_hall.staff.entity.StaffType;

public record StaffResponse(Long staffId, String name, String role, StaffType type, String contact) {

    public static StaffResponse from(Staff s) {
        return new StaffResponse(s.getStaffId(), s.getName(), s.getRole(), s.getType(), s.getContact());
    }
}
