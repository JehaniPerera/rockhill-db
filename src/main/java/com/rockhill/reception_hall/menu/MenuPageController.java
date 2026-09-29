package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/menus")
public class MenuPageController {

    private final MenuService menuService;

    public MenuPageController(MenuService menuService) {
        this.menuService = menuService;
    }

    // Show the page: table of menus + an "add menu" form
    @GetMapping
    public String list(Model model) {
        model.addAttribute("menus", menuService.getAllMenus());
        model.addAttribute("newMenu", new Menu());
        return "menu/list";          // = templates/menu/list.html
    }

    // Form submits here
    @PostMapping
    public String create(@ModelAttribute Menu menu) {
        menuService.saveMenu(menu);
        return "redirect:/admin/menus";   // reload the list
    }

    // Delete button
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        menuService.deleteMenu(id);
        return "redirect:/admin/menus";
    }
}