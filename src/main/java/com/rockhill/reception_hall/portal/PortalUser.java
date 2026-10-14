package com.rockhill.reception_hall.portal;

import jakarta.persistence.*;

@Entity
@Table(name = "portal_user", uniqueConstraints = @UniqueConstraint(name = "uq_portal_user_email", columnNames = "email"))
public class PortalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PortalRole role;

    @Column(name = "customer_id")
    private Long customerId;

    protected PortalUser() {}

    public PortalUser(String email, String passwordHash, String fullName, String phone, PortalRole role, Long customerId) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.customerId = customerId;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public PortalRole getRole() { return role; }
    public Long getCustomerId() { return customerId; }
}
