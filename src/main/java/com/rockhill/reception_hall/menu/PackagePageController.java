package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/admin/packages")
public class PackagePageController {

    private final PackageService packageService;

    public PackagePageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("packages", packageService.getAllPackages());
        model.addAttribute("newPackage", new Package());
        return "package/list";
    }

    @PostMapping
    public String create(@ModelAttribute Package pkg) {
        packageService.savePackage(pkg);
        return "redirect:/admin/packages";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Package> pkg = packageService.getPackageById(id);
        if (pkg.isEmpty()) {
            return "redirect:/admin/packages";
        }
        model.addAttribute("pkg", pkg.get());
        return "package/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id, @ModelAttribute Package form) {
        packageService.getPackageById(id).ifPresent(existing -> {
            existing.setName(form.getName());
            existing.setDescription(form.getDescription());
            packageService.savePackage(existing);
        });
        return "redirect:/admin/packages";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        packageService.deletePackage(id);
        return "redirect:/admin/packages";
    }
}