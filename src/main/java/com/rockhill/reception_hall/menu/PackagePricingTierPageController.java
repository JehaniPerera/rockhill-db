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

    @GetMapping("/{tierId}/edit")
    public String editForm(@PathVariable Integer packageId,
                           @PathVariable Integer tierId, Model model) {
        Optional<Package> pkg = packageService.getPackageById(packageId);
        Optional<PackagePricingTier> tier = tierService.getTierById(tierId);
        if (pkg.isEmpty() || tier.isEmpty()) {
            return "redirect:/admin/packages/" + packageId + "/tiers";
        }
        model.addAttribute("pkg", pkg.get());
        model.addAttribute("tier", tier.get());
        return "tier/edit";
    }

    @PostMapping("/{tierId}/edit")
    public String update(@PathVariable Integer packageId,
                         @PathVariable Integer tierId,
                         @ModelAttribute PackagePricingTier form) {
        tierService.getTierById(tierId).ifPresent(existing -> {
            existing.setTierNumber(form.getTierNumber());
            existing.setMinGuests(form.getMinGuests());
            existing.setMaxGuests(form.getMaxGuests());
            existing.setTotalPrice(form.getTotalPrice());
            tierService.saveTier(existing);
        });
        return "redirect:/admin/packages/" + packageId + "/tiers";
    }

    @PostMapping("/{tierId}/delete")
    public String delete(@PathVariable Integer packageId, @PathVariable Integer tierId) {
        tierService.deleteTier(tierId);
        return "redirect:/admin/packages/" + packageId + "/tiers";
    }
}