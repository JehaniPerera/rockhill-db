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
    public List<AddOn> getAllAddOns() {
        return addOnService.getAllAddOns();
    }

    @GetMapping("/{id}")
    public AddOn getAddOnById(@PathVariable Integer id) {
        return addOnService.getAddOnById(id).orElse(null);
    }

    @PostMapping
    public AddOn createAddOn(@RequestBody AddOn addOn) {
        return addOnService.saveAddOn(addOn);
    }

    @DeleteMapping("/{id}")
    public void deleteAddOn(@PathVariable Integer id) {
        addOnService.deleteAddOn(id);
    }
}
