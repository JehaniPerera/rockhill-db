package com.rockhill.hallbooking.dto;

import com.rockhill.hallbooking.entity.Staff;
import com.rockhill.hallbooking.entity.StaffType;

public record StaffResponse(Long staffId, String name, String role, StaffType type, String contact) {

    public static StaffResponse from(Staff s) {
        return new StaffResponse(s.getStaffId(), s.getName(), s.getRole(), s.getType(), s.getContact());
    }
}
