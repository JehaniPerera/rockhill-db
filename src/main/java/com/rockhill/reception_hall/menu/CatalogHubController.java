package com.rockhill.reception_hall.menu;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/catalog")
public class CatalogHubController {

    @GetMapping
    public String hub() {
        return "catalog/hub";
    }
}
