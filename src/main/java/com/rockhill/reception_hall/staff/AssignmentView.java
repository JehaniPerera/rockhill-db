package com.rockhill.reception_hall.staff;

public class AssignmentView {
    private int staffId;
    private AssignmentStatus status;

    public AssignmentView(int staffId, AssignmentStatus status) {
        this.staffId = staffId;
        this.status = status;
    }

    public int getStaffId() { return staffId; }
    public AssignmentStatus getStatus() { return status; }
}
