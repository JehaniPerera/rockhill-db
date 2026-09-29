package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/menus")
public class MenuBrowseController {

    @Autowired
    private MenuService menuService;

    @GetMapping
    public String browse(Model model) {
        model.addAttribute("menus", menuService.getAllMenus());
        return "menu/browse";
    }
}