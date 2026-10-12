package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    public String create(@ModelAttribute Package pkg, RedirectAttributes redirect) {
        try {
            packageService.savePackage(pkg);
            redirect.addFlashAttribute("success", "Package saved.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
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
    public String update(@PathVariable Integer id, @ModelAttribute Package form, RedirectAttributes redirect) {
        form.setPackageId(id);
        try {
            packageService.savePackage(form);
            redirect.addFlashAttribute("success", "Package updated.");
            return "redirect:/admin/packages";
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
            return "redirect:/admin/packages/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            packageService.deletePackage(id);
            redirect.addFlashAttribute("success", "Package deleted.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/packages";
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            packageService.setPackageActive(id, false);
            redirect.addFlashAttribute("success", "Package disabled. New customers can no longer choose it.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/packages";
    }

    @PostMapping("/{id}/enable")
    public String enable(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            packageService.setPackageActive(id, true);
            redirect.addFlashAttribute("success", "Package enabled again.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/packages";
    }
}
