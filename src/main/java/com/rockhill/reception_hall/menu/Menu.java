package com.rockhill.reception_hall.menu;

import jakarta.persistence.*;

@Entity
@Table(name = "Menu")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer menuId;

    private String name;
    private Double pricePerGuest;

    private Boolean active = true;

    public Integer getMenuId() { return menuId; }
    public void setMenuId(Integer menuId) { this.menuId = menuId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPricePerGuest() { return pricePerGuest; }
    public void setPricePerGuest(Double pricePerGuest) { this.pricePerGuest = pricePerGuest; }

    public Boolean getActive() { return active == null || active; }
    public void setActive(Boolean active) { this.active = active; }
}
