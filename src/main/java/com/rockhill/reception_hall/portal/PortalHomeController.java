package com.rockhill.reception_hall.portal;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** "/" sends each person to their own home page. */
@Controller
public class PortalHomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return admin ? "redirect:/admin/index.html" : "redirect:/index.html";
    }
}
