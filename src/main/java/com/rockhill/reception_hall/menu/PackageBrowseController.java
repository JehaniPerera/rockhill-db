package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/packages")
public class PackageBrowseController {

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackagePricingTierService tierService;

    @GetMapping
    public String browse(Model model) {
        List<Package> packages = packageService.getActivePackages();

        Map<Integer, List<PackagePricingTier>> tiersByPackage = new HashMap<>();
        for (Package p : packages) {
            tiersByPackage.put(p.getPackageId(), tierService.getTiersForPackage(p.getPackageId()));
        }

        model.addAttribute("packages", packages);
        model.addAttribute("tiersByPackage", tiersByPackage);
        return "package/browse";
    }
}