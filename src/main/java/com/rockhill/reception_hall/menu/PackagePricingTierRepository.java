package com.rockhill.reception_hall.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackagePricingTierRepository extends JpaRepository<PackagePricingTier, Integer> {
    List<PackagePricingTier> findByPkg_PackageId(Integer packageId);
}
