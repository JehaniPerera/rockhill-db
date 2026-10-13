package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
@RequestMapping("/admin/addons")
public class AddOnPageController {

    private final AddOnService addOnService;

    public AddOnPageController(AddOnService addOnService) {
        this.addOnService = addOnService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("addons", addOnService.getAllAddOns());
        model.addAttribute("newAddOn", new AddOn());
        return "addon/list";
    }

    @PostMapping
    public String create(@ModelAttribute AddOn addOn, RedirectAttributes redirect) {
        try {
            addOnService.saveAddOn(addOn);
            redirect.addFlashAttribute("success", "Add-on saved.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/addons";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<AddOn> addOn = addOnService.getAddOnById(id);
        if (addOn.isEmpty()) {
            return "redirect:/admin/addons";
        }
        model.addAttribute("addOn", addOn.get());
        return "addon/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id, @ModelAttribute AddOn form, RedirectAttributes redirect) {
        form.setAddonId(id);
        try {
            addOnService.saveAddOn(form);
            redirect.addFlashAttribute("success", "Add-on updated.");
            return "redirect:/admin/addons";
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
            return "redirect:/admin/addons/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            addOnService.deleteAddOn(id);
            redirect.addFlashAttribute("success", "Add-on deleted.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/addons";
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            addOnService.setAddOnActive(id, false);
            redirect.addFlashAttribute("success", "Add-on disabled. New customers can no longer choose it.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/addons";
    }

    @PostMapping("/{id}/enable")
    public String enable(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            addOnService.setAddOnActive(id, true);
            redirect.addFlashAttribute("success", "Add-on enabled again.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/addons";
    }
}
