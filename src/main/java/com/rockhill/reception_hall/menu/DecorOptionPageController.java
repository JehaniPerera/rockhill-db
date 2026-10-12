package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    public String create(@ModelAttribute DecorOption decorOption, RedirectAttributes redirect) {
        try {
            decorOptionService.saveDecorOption(decorOption);
            redirect.addFlashAttribute("success", "Decor option saved.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
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
    public String update(@PathVariable Integer id, @ModelAttribute DecorOption form, RedirectAttributes redirect) {
        form.setDecorId(id);
        try {
            decorOptionService.saveDecorOption(form);
            redirect.addFlashAttribute("success", "Decor option updated.");
            return "redirect:/admin/decor";
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
            return "redirect:/admin/decor/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            decorOptionService.deleteDecorOption(id);
            redirect.addFlashAttribute("success", "Decor option deleted.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/decor";
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            decorOptionService.setDecorOptionActive(id, false);
            redirect.addFlashAttribute("success", "Decor option disabled. New customers can no longer choose it.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/decor";
    }

    @PostMapping("/{id}/enable")
    public String enable(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            decorOptionService.setDecorOptionActive(id, true);
            redirect.addFlashAttribute("success", "Decor option enabled again.");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/decor";
    }
}
