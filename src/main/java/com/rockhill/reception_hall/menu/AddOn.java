package com.rockhill.reception_hall.menu;

import jakarta.persistence.*;

@Entity
@Table(name = "AddOn")
public class AddOn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer addonId;

    private String name;
    private Double price;
    private Boolean includedInPackage;

    public Integer getAddonId() { return addonId; }
    public void setAddonId(Integer addonId) { this.addonId = addonId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Boolean getIncludedInPackage() { return includedInPackage; }
    public void setIncludedInPackage(Boolean includedInPackage) {this.includedInPackage = includedInPackage; }
}
