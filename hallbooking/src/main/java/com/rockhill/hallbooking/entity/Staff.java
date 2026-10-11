package com.rockhill.hallbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Table Staff (staff_id, name, role, type, contact) + composition with StaffAvailability. */
@Entity
@Table(name = "Staff")
@Getter
@Setter
@NoArgsConstructor
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
}
