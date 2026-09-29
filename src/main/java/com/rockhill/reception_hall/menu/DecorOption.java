package com.rockhill.reception_hall.menu;

import jakarta.persistence.*;

@Entity
@Table(name = "DecorOption")
public class DecorOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer decorId;

    private String category;
    private String name;

    // NEW: decor now has its own price. Weddings never pay for decor at
    // all (it's part of the package); non-wedding events always do.
    private Double price;

    public Integer getDecorId() { return decorId; }
    public void setDecorId(Integer decorId) { this.decorId = decorId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
