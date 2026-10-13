package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import static com.rockhill.reception_hall.menu.CatalogValidation.*;

@Service
public class AddOnService {

    @Autowired
    private AddOnRepository addOnRepository;

    public List<AddOn> getAllAddOns() {
        return addOnRepository.findAll();
    }

    public List<AddOn> getActiveAddOns() {
        return addOnRepository.findAll().stream().filter(AddOn::getActive).toList();
    }

    public List<AddOn> getAddOnsForEventType(String eventType) {
        // Function 1 uses WEDDING, the catalog used "Wedding": accept both.
        if ("Wedding".equalsIgnoreCase(eventType)) {
            return getActiveAddOns();
        }
        return getActiveAddOns().stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsWeddingExclusive()))
                .toList();
    }

    public AddOn setAddOnActive(Integer id, boolean active) {
        AddOn addOn = addOnRepository.findById(id).orElseThrow(() -> notFound("Add-on not found: " + id));
        addOn.setActive(active);
        return addOnRepository.save(addOn);
    }

    public Optional<AddOn> getAddOnById(Integer id) {
        return addOnRepository.findById(id);
    }

    public AddOn saveAddOn(AddOn addOn) {
        addOn.setName(requireText(addOn.getName(), "Add-on name"));
        requireNonNegative(addOn.getPrice(), "Price");

        String unit = addOn.getUnitType() == null || addOn.getUnitType().isBlank()
                ? "per item" : addOn.getUnitType().trim().toLowerCase();
        if (!unit.equals("per item") && !unit.equals("per guest")) {
            throw bad("Unit type must be \"per item\" or \"per guest\".");
        }
        addOn.setUnitType(unit);
        if (addOn.getIncludedInPackage() == null) addOn.setIncludedInPackage(false);
        if (addOn.getIsWeddingExclusive() == null) addOn.setIsWeddingExclusive(false);

        Integer id = addOn.getAddonId();
        if (id == null) {
            addOn.setActive(true);
        } else {
            AddOn existing = addOnRepository.findById(id)
                    .orElseThrow(() -> notFound("Add-on not found: " + id));
            addOn.setActive(existing.getActive());
        }
        boolean duplicate = addOnRepository.findAll().stream()
                .anyMatch(a -> !a.getAddonId().equals(id) && sameName(a.getName(), addOn.getName()));
        if (duplicate) {
            throw conflict("An add-on named \"" + addOn.getName() + "\" already exists.");
        }
        return addOnRepository.save(addOn);
    }

    public void deleteAddOn(Integer id) {
        if (!addOnRepository.existsById(id)) {
            throw notFound("Add-on not found: " + id);
        }
        try {
            addOnRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw conflict("This add-on is used by existing bookings and cannot be deleted. Disable it instead so new customers can no longer choose it.");
        }
    }
}
