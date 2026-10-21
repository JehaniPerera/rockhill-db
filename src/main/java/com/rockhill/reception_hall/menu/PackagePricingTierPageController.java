package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
                         @ModelAttribute PackagePricingTier tier,
                         RedirectAttributes redirect) {
        Optional<Package> pkg = packageService.getPackageById(packageId);
        if (pkg.isEmpty()) {
            return "redirect:/admin/packages";
        }
        tier.setTierId(null);
        tier.setPkg(pkg.get());
        try {
            tierService.saveTier(tier);
            redirect.addFlashAttribute("success", "Pricing tier saved.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
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
                         @ModelAttribute PackagePricingTier form,
                         RedirectAttributes redirect) {
        String listUrl = "redirect:/admin/packages/" + packageId + "/tiers";
        Optional<Package> pkg = packageService.getPackageById(packageId);
        Optional<PackagePricingTier> existing = tierService.getTierById(tierId);
        if (pkg.isEmpty() || existing.isEmpty()
                || !existing.get().getPkg().getPackageId().equals(packageId)) {
            redirect.addFlashAttribute("error", "Pricing tier not found.");
            return listUrl;
        }
        form.setTierId(tierId);
        form.setPkg(pkg.get());
        // keep the current tier number if the edit form does not send one
        if (form.getTierNumber() == null) {
            form.setTierNumber(existing.get().getTierNumber());
        }
        try {
            tierService.saveTier(form);
            redirect.addFlashAttribute("success", "Pricing tier updated.");
            return listUrl;
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
            return listUrl + "/" + tierId + "/edit";
        }
    }

    @PostMapping("/{tierId}/delete")
    public String delete(@PathVariable Integer packageId, @PathVariable Integer tierId,
                         RedirectAttributes redirect) {
        try {
            tierService.deleteTier(tierId);
            redirect.addFlashAttribute("success", "Pricing tier deleted.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/packages/" + packageId + "/tiers";
    }
}
