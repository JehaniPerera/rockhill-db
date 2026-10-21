package com.rockhill.reception_hall.menu;

import jakarta.persistence.*;

@Entity
@Table(name = "PackagePricingTier")
public class PackagePricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tierId;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private Package pkg;

    private Integer tierNumber;

    private Integer minGuests;
    private Integer maxGuests;
    private Double totalPrice;

    public Integer getTierId() { return tierId; }
    public void setTierId(Integer tierId) { this.tierId = tierId; }

    public Package getPkg() { return pkg; }
    public void setPkg(Package pkg) { this.pkg = pkg; }

    public Integer getTierNumber() { return tierNumber; }
    public void setTierNumber(Integer tierNumber) { this.tierNumber = tierNumber; }

    public Integer getMinGuests() { return minGuests; }
    public void setMinGuests(Integer minGuests) { this.minGuests = minGuests; }

    public Integer getMaxGuests() { return maxGuests; }
    public void setMaxGuests(Integer maxGuests) { this.maxGuests = maxGuests; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}