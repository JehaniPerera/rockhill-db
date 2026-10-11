package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin/menus")
public class MenuPageController {

    private final MenuService menuService;

    public MenuPageController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("menus", menuService.getAllMenus());
        model.addAttribute("newMenu", new Menu());
        return "menu/list";
    }

    @PostMapping
    public String create(@ModelAttribute Menu menu) {
        menuService.saveMenu(menu);
        return "redirect:/admin/menus";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Menu> menu = menuService.getMenuById(id);
        if (menu.isEmpty()) {
            return "redirect:/admin/menus";
        }
        model.addAttribute("menu", menu.get());
        return "menu/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id, @ModelAttribute Menu form) {
        menuService.getMenuById(id).ifPresent(existing -> {
            existing.setName(form.getName());
            existing.setPricePerGuest(form.getPricePerGuest());
            menuService.saveMenu(existing);
        });
        return "redirect:/admin/menus";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        menuService.deleteMenu(id);
        return "redirect:/admin/menus";
    }
}