package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import static com.rockhill.reception_hall.menu.CatalogValidation.*;

@Service
public class PackagePricingTierService {

    @Autowired
    private PackagePricingTierRepository tierRepository;

    @Autowired
    private PackageRepository packageRepository;

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
        if (tier.getPkg() == null || tier.getPkg().getPackageId() == null) {
            throw bad("Package is required.");
        }
        Package pkg = packageRepository.findById(tier.getPkg().getPackageId())
                .orElseThrow(() -> bad("Package not found: " + tier.getPkg().getPackageId()));
        tier.setPkg(pkg);

        if (tier.getMinGuests() == null || tier.getMaxGuests() == null) {
            throw bad("Minimum and maximum guests are required.");
        }
        if (tier.getMinGuests() < 1) {
            throw bad("Minimum guests must be at least 1.");
        }
        if (tier.getMaxGuests() < tier.getMinGuests()) {
            throw bad("Maximum guests cannot be less than minimum guests.");
        }
        requireNonNegative(tier.getTotalPrice(), "Total price");

        Integer id = tier.getTierId();
        if (id != null && !tierRepository.existsById(id)) {
            throw notFound("Pricing tier not found: " + id);
        }

        List<PackagePricingTier> others = tierRepository.findByPkg_PackageId(pkg.getPackageId()).stream()
                .filter(t -> !t.getTierId().equals(id))
                .toList();

        for (PackagePricingTier o : others) {
            boolean overlap = tier.getMinGuests() <= o.getMaxGuests() && o.getMinGuests() <= tier.getMaxGuests();
            if (overlap) {
                throw conflict("Guest range " + tier.getMinGuests() + "-" + tier.getMaxGuests()
                        + " overlaps an existing tier of this package (" + o.getMinGuests() + "-" + o.getMaxGuests() + ").");
            }
        }

        if (tier.getTierNumber() == null) {
            tier.setTierNumber(others.stream()
                    .mapToInt(t -> t.getTierNumber() == null ? 0 : t.getTierNumber()).max().orElse(0) + 1);
        } else {
            if (tier.getTierNumber() < 1) {
                throw bad("Tier number must be at least 1.");
            }
            boolean numberUsed = others.stream().anyMatch(t -> tier.getTierNumber().equals(t.getTierNumber()));
            if (numberUsed) {
                throw conflict("Tier number " + tier.getTierNumber() + " is already used in this package.");
            }
        }
        return tierRepository.save(tier);
    }

    public void deleteTier(Integer id) {
        if (!tierRepository.existsById(id)) {
            throw notFound("Pricing tier not found: " + id);
        }
        tierRepository.deleteById(id);
    }

    public Optional<PackagePricingTier> findTierForGuestCount(Integer packageId, Integer guestCount) {
        return getTiersForPackage(packageId).stream()
                .filter(t -> guestCount >= t.getMinGuests() && guestCount <= t.getMaxGuests())
                .findFirst();
    }
}
