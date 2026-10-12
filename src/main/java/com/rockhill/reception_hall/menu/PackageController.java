package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @GetMapping
    public List<Package> getAllPackages(@RequestParam(required = false) Boolean includeInactive) {
        return Boolean.TRUE.equals(includeInactive) ? packageService.getAllPackages() : packageService.getActivePackages();
    }

    @GetMapping("/{id}")
    public Package getPackageById(@PathVariable Integer id) {
        return packageService.getPackageById(id).orElse(null);
    }

    @PostMapping
    public Package createPackage(@RequestBody Package pkg) {
        return packageService.savePackage(pkg);
    }

    @PutMapping("/{id}")
    public Package updatePackage(@PathVariable Integer id, @RequestBody Package pkg) {
        pkg.setPackageId(id);
        return packageService.savePackage(pkg);
    }

    @DeleteMapping("/{id}")
    public void deletePackage(@PathVariable Integer id) {
        packageService.deletePackage(id);
    }
}