package com.rockhill.reception_hall.staff;

import jakarta.persistence.*;

@Entity
@Table(name = "Staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private int id;

    private String name;
    private String role;
    private String type;
    private String contact;

    protected Staff() { }

    public Staff(String name, String role, String type, String contact) {
        this.name = name;
        this.role = role;
        this.type = type;
        this.contact = contact;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getType() { return type; }
    public String getContact() { return contact; }

    public String toString() {
        return id + " - " + name + " (" + role + ")";
    }
}
