package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        packageService.deletePackage(id);
        return "redirect:/admin/packages";
    }
}