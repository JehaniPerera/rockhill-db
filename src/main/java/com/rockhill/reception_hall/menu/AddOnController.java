package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/addons")
public class AddOnController {
    @Autowired
    private AddOnService addOnService;

    @GetMapping
    public List<AddOn> getAllAddOns(@RequestParam(required = false) String eventType,
                                    @RequestParam(required = false) Boolean includeInactive) {
        if (eventType == null) {
            return Boolean.TRUE.equals(includeInactive) ? addOnService.getAllAddOns() : addOnService.getActiveAddOns();
        }
        return addOnService.getAddOnsForEventType(eventType);
    }

    @GetMapping("/{id}")
    public AddOn getAddOnById(@PathVariable Integer id) {
        return addOnService.getAddOnById(id).orElse(null);
    }

    @PostMapping
    public AddOn createAddOn(@RequestBody AddOn addOn) {
        return addOnService.saveAddOn(addOn);
    }

    @PutMapping("/{id}")
    public AddOn updateAddOn(@PathVariable Integer id, @RequestBody AddOn addOn) {
        addOn.setAddonId(id);
        return addOnService.saveAddOn(addOn);
    }

    @DeleteMapping("/{id}")
    public void deleteAddOn(@PathVariable Integer id) {
        addOnService.deleteAddOn(id);
    }
}
