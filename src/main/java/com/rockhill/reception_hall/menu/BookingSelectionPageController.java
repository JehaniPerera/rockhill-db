package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/booking/{bookingId}")
public class BookingSelectionPageController {

    @GetMapping("/select/addons")
    public String addons(@PathVariable Long bookingId, Model model) {
        model.addAttribute("bookingId", bookingId);
        return "selection/addons";
    }

    @GetMapping("/select/decor")
    public String decor(@PathVariable Long bookingId, Model model) {
        model.addAttribute("bookingId", bookingId);
        return "selection/decor";
    }

    @GetMapping("/summary")
    public String summary(@PathVariable Long bookingId, Model model) {
        model.addAttribute("bookingId", bookingId);
        return "selection/summary";
    }
}