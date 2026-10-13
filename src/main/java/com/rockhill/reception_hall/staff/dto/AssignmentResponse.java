package com.rockhill.reception_hall.staff.dto;

import com.rockhill.reception_hall.staff.entity.StaffAssignment;

import java.time.LocalDate;

public record AssignmentResponse(Long bookingId, LocalDate assignedDate, StaffResponse staff) {

    public static AssignmentResponse from(StaffAssignment a) {
        return new AssignmentResponse(a.getId().getBookingId(), a.getAssignedDate(),
                StaffResponse.from(a.getStaff()));
    }
}
