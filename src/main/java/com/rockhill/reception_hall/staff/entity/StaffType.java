package com.rockhill.reception_hall.staff.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Staff.type ENUM('Internal','Vendor'). JSON uses the same words: "Internal" / "Vendor". */
public enum StaffType {
    INTERNAL("Internal"),
    VENDOR("Vendor");

    private final String dbValue;

    StaffType(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    @JsonCreator
    public static StaffType fromDbValue(String value) {
        for (StaffType t : values()) {
            if (t.dbValue.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown staff type: " + value);
    }
}
