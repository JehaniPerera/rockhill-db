package com.rockhill.reception_hall.staff.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Convert(converter = StaffTypeConverter.class)
    @Column(name = "type", nullable = false)
    private StaffType type;

    @Column(name = "contact", length = 100)
    private String contact;

    /** blocked_dates (composition): dates this staff/vendor is unavailable. */
    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffAvailability> blockedDates = new ArrayList<>();

    public Staff() {
    }

    public Long getStaffId() {
        return staffId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public StaffType getType() {
        return type;
    }

    public String getContact() {
        return contact;
    }

    public List<StaffAvailability> getBlockedDates() {
        return blockedDates;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setType(StaffType type) {
        this.type = type;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setBlockedDates(List<StaffAvailability> blockedDates) {
        this.blockedDates = blockedDates;
    }
}
