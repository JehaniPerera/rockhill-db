package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public String create(@ModelAttribute AddOn addOn) {
        addOnService.saveAddOn(addOn);
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
    public String update(@PathVariable Integer id, @ModelAttribute AddOn form) {
        addOnService.getAddOnById(id).ifPresent(existing -> {
            existing.setName(form.getName());
            existing.setPrice(form.getPrice());
            existing.setUnitType(form.getUnitType());
            existing.setIncludedInPackage(form.getIncludedInPackage());
            existing.setIsWeddingExclusive(form.getIsWeddingExclusive());
            addOnService.saveAddOn(existing);
        });
        return "redirect:/admin/addons";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        addOnService.deleteAddOn(id);
        return "redirect:/admin/addons";
    }
}