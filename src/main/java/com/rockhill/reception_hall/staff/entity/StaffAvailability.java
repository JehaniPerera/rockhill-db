package com.rockhill.reception_hall.staff.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Table StaffAvailability (NEW - see sql/StaffAvailability.sql). */
@Entity
@Table(name = "StaffAvailability")
public class StaffAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long availabilityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "unavailable_date", nullable = false)
    private LocalDate unavailableDate;

    @Column(name = "reason", length = 255)
    private String reason;

    public StaffAvailability() {
    }

    public Long getAvailabilityId() {
        return availabilityId;
    }

    public Staff getStaff() {
        return staff;
    }

    public LocalDate getUnavailableDate() {
        return unavailableDate;
    }

    public String getReason() {
        return reason;
    }

    public void setAvailabilityId(Long availabilityId) {
        this.availabilityId = availabilityId;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public void setUnavailableDate(LocalDate unavailableDate) {
        this.unavailableDate = unavailableDate;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
