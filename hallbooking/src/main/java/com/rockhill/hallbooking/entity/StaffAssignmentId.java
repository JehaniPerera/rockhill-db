package com.rockhill.hallbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Composite primary key (booking_id, staff_id) of StaffAssignment. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StaffAssignmentId implements Serializable {

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "staff_id")
    private Long staffId;
}
