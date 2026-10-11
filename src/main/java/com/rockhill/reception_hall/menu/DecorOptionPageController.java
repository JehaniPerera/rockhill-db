package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<DecorOption> decor = decorOptionService.getDecorOptionById(id);
        if (decor.isEmpty()) {
            return "redirect:/admin/decor";
        }
        model.addAttribute("decor", decor.get());
        model.addAttribute("categories", decorOptionService.getAllDecorOptions().stream()
                .map(DecorOption::getCategory)
                .distinct()
                .toList());
        return "decor/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Integer id, @ModelAttribute DecorOption form) {
        decorOptionService.getDecorOptionById(id).ifPresent(existing -> {
            existing.setCategory(form.getCategory());
            existing.setName(form.getName());
            existing.setPrice(form.getPrice());
            existing.setIsSingular(form.getIsSingular());
            decorOptionService.saveDecorOption(existing);
        });
        return "redirect:/admin/decor";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        decorOptionService.deleteDecorOption(id);
        return "redirect:/admin/decor";
    }
}