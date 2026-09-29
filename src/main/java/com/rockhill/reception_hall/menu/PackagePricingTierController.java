package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/package-pricing-tiers")
public class PackagePricingTierController {

    @Autowired
    private PackagePricingTierService tierService;

    @GetMapping
    public List<PackagePricingTier> getAllTiers() {
        return tierService.getAllTiers();
    }

    @GetMapping("/package/{packageId}")
    public List<PackagePricingTier> getTiersForPackage(@PathVariable Integer packageId) {
        return tierService.getTiersForPackage(packageId);
    }

    @PostMapping
    public PackagePricingTier createTier(@RequestBody PackagePricingTier tier) {
        return tierService.saveTier(tier);
    }

    @PutMapping("/{id}")
    public PackagePricingTier updateTier(@PathVariable Integer id, @RequestBody PackagePricingTier tier) {
        tier.setTierId(id);
        return tierService.saveTier(tier);
    }

    @DeleteMapping("/{id}")
    public void deleteTier(@PathVariable Integer id) {
        tierService.deleteTier(id);
    }
}
