package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/packages/{packageId}/tiers")
public class PackagePricingTierPageController {

    private final PackageService packageService;
    private final PackagePricingTierService tierService;

    public PackagePricingTierPageController(PackageService packageService,
                                            PackagePricingTierService tierService) {
        this.packageService = packageService;
        this.tierService = tierService;
    }

    @GetMapping
    public String list(@PathVariable Integer packageId, Model model) {
        Optional<Package> pkg = packageService.getPackageById(packageId);
        if (pkg.isEmpty()) {
            return "redirect:/admin/packages";
        }

        List<PackagePricingTier> tiers = tierService.getTiersForPackage(packageId).stream()
                .sorted(Comparator.comparing(PackagePricingTier::getMinGuests,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();

        model.addAttribute("pkg", pkg.get());
        model.addAttribute("tiers", tiers);
        model.addAttribute("newTier", new PackagePricingTier());
        return "tier/list";
    }

    @PostMapping
    public String create(@PathVariable Integer packageId,
                         @ModelAttribute PackagePricingTier tier) {
        Optional<Package> pkg = packageService.getPackageById(packageId);
        if (pkg.isPresent()) {
            tier.setPkg(pkg.get());
            tierService.saveTier(tier);
        }
        return "redirect:/admin/packages/" + packageId + "/tiers";
    }

    @PostMapping("/{tierId}/delete")
    public String delete(@PathVariable Integer packageId, @PathVariable Integer tierId) {
        tierService.deleteTier(tierId);
        return "redirect:/admin/packages/" + packageId + "/tiers";
    }
}