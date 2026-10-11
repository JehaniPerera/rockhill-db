package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/decor")
public class DecorOptionPageController {

    private final DecorOptionService decorOptionService;

    public DecorOptionPageController(DecorOptionService decorOptionService) {
        this.decorOptionService = decorOptionService;
    }

    @GetMapping
    public String list(Model model) {
        List<DecorOption> options = decorOptionService.getAllDecorOptions().stream()
                .sorted(Comparator.comparing(DecorOption::getCategory,
                                Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(DecorOption::getName,
                                Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        List<String> categories = options.stream()
                .map(DecorOption::getCategory)
                .distinct()
                .toList();

        model.addAttribute("options", options);
        model.addAttribute("categories", categories);
        model.addAttribute("newDecor", new DecorOption());
        return "decor/list";
    }

    @PostMapping
    public String create(@ModelAttribute DecorOption decorOption) {
        decorOptionService.saveDecorOption(decorOption);
        return "redirect:/admin/decor";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        decorOptionService.deleteDecorOption(id);
        return "redirect:/admin/decor";
    }
}