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
    private Boolean includedInPackage = false;

    private String unitType = "per item";

    @Column(name = "is_wedding_exclusive")
    private Boolean isWeddingExclusive = false;

    private Boolean active = true;

    public Integer getAddonId() { return addonId; }
    public void setAddonId(Integer addonId) { this.addonId = addonId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Boolean getIncludedInPackage() { return includedInPackage; }
    public void setIncludedInPackage(Boolean includedInPackage) { this.includedInPackage = includedInPackage; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public Boolean getIsWeddingExclusive() { return isWeddingExclusive; }
    public void setIsWeddingExclusive(Boolean isWeddingExclusive) { this.isWeddingExclusive = isWeddingExclusive; }

    public Boolean getActive() { return active == null || active; }
    public void setActive(Boolean active) { this.active = active; }
}
