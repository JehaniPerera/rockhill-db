package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        addOnService.deleteAddOn(id);
        return "redirect:/admin/addons";
    }
}