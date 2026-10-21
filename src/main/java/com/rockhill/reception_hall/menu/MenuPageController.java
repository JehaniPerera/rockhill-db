package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    public String create(@ModelAttribute Menu menu, RedirectAttributes redirect) {
        try {
            menuService.saveMenu(menu);
            redirect.addFlashAttribute("success", "Menu saved.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
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
    public String update(@PathVariable Integer id, @ModelAttribute Menu form, RedirectAttributes redirect) {
        form.setMenuId(id);
        try {
            menuService.saveMenu(form);
            redirect.addFlashAttribute("success", "Menu updated.");
            return "redirect:/admin/menus";
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
            return "redirect:/admin/menus/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            menuService.deleteMenu(id);
            redirect.addFlashAttribute("success", "Menu deleted.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/menus";
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            menuService.setMenuActive(id, false);
            redirect.addFlashAttribute("success", "Menu disabled. New customers can no longer choose it.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/menus";
    }

    @PostMapping("/{id}/enable")
    public String enable(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            menuService.setMenuActive(id, true);
            redirect.addFlashAttribute("success", "Menu enabled again.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/menus";
    }
}
