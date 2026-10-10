package com.rockhill.hallbooking.dto;

import com.rockhill.hallbooking.entity.StaffAssignment;

import java.time.LocalDate;

public record AssignmentResponse(Long bookingId, LocalDate assignedDate, StaffResponse staff) {

    public static AssignmentResponse from(StaffAssignment a) {
        return new AssignmentResponse(a.getId().getBookingId(), a.getAssignedDate(),
                StaffResponse.from(a.getStaff()));
    }
}
