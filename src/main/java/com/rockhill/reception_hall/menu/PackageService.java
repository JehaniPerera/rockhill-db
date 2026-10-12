package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import static com.rockhill.reception_hall.menu.CatalogValidation.*;

@Service
public class PackageService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired(required = false)
    private CatalogUsageChecker usageChecker;

    public List<Package> getAllPackages() {
        return packageRepository.findAll();
    }

    public List<Package> getActivePackages() {
        return packageRepository.findAll().stream().filter(Package::getActive).toList();
    }

    public Optional<Package> getPackageById(Integer id) {
        return packageRepository.findById(id);
    }

    public Package requireActivePackage(Integer id) {
        Package pkg = packageRepository.findById(id).orElseThrow(() -> bad("Package not found: " + id));
        if (!pkg.getActive()) {
            throw bad("The package \"" + pkg.getName() + "\" is no longer available.");
        }
        return pkg;
    }

    public Package setPackageActive(Integer id, boolean active) {
        Package pkg = packageRepository.findById(id).orElseThrow(() -> notFound("Package not found: " + id));
        pkg.setActive(active);
        return packageRepository.save(pkg);
    }

    public Package savePackage(Package pkg) {
        pkg.setName(requireText(pkg.getName(), "Package name"));

        Integer id = pkg.getPackageId();
        if (id == null) {
            pkg.setActive(true);
        } else {
            Package existing = packageRepository.findById(id)
                    .orElseThrow(() -> notFound("Package not found: " + id));
            pkg.setActive(existing.getActive());
        }
        boolean duplicate = packageRepository.findAll().stream()
                .anyMatch(p -> !p.getPackageId().equals(id) && sameName(p.getName(), pkg.getName()));
        if (duplicate) {
            throw conflict("A package named \"" + pkg.getName() + "\" already exists.");
        }
        return packageRepository.save(pkg);
    }

    public void deletePackage(Integer id) {
        if (!packageRepository.existsById(id)) {
            throw notFound("Package not found: " + id);
        }
        if (usageChecker != null && usageChecker.isPackageUsed(id)) {
            throw conflict("This package is used by existing bookings and cannot be deleted. Disable it instead so new customers can no longer choose it.");
        }
        try {
            packageRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw conflict("This package still has pricing tiers or is used by bookings, so it cannot be deleted. Disable it instead so new customers can no longer choose it.");
        }
    }
}
