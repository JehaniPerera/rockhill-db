package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PackagePricingTierService {

    @Autowired
    private PackagePricingTierRepository tierRepository;

    public List<PackagePricingTier> getAllTiers() {
        return tierRepository.findAll();
    }

    public List<PackagePricingTier> getTiersForPackage(Integer packageId) {
        return tierRepository.findByPkg_PackageId(packageId);
    }

    public Optional<PackagePricingTier> getTierById(Integer id) {
        return tierRepository.findById(id);
    }

    public PackagePricingTier saveTier(PackagePricingTier tier) {
        return tierRepository.save(tier);
    }

    public void deleteTier(Integer id) {
        tierRepository.deleteById(id);
    }

    public Optional<PackagePricingTier> findTierForGuestCount(Integer packageId, Integer guestCount) {
        return getTiersForPackage(packageId).stream()
                .filter(t -> guestCount >= t.getMinGuests() && guestCount <= t.getMaxGuests())
                .findFirst();
    }
}
