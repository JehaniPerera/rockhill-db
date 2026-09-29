package com.rockhill.reception_hall.staff;

public class Staff {
    private int id;
    private String name;
    private String role;
    private String type;
    private String contact;

    public Staff(int id, String name, String role, String type, String contact) {
        this.id = id;
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
