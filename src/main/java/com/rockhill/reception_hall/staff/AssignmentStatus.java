package com.rockhill.reception_hall.staff;

public enum AssignmentStatus {
    PENDING_CONFIRMATION,   // client picked, manager has not called yet
    CONFIRMED,              // manager called and this person/team is available
    UNAVAILABLE             // manager called and this person/team is NOT available
}
