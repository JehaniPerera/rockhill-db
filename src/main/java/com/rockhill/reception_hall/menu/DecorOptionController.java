package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decor-options")
public class DecorOptionController {

    @Autowired
    private DecorOptionService decorOptionService;

    @GetMapping
    public List<DecorOption> getAllDecorOptions() {
        return decorOptionService.getAllDecorOptions();
    }

    @GetMapping("/{id}")
    public DecorOption getDecorOptionById(@PathVariable Integer id) {
        return decorOptionService.getDecorOptionById(id).orElse(null);
    }

    @PostMapping
    public DecorOption createDecorOption(@RequestBody DecorOption decorOption) {
        return decorOptionService.saveDecorOption(decorOption);
    }

    @PutMapping("/{id}")
    public DecorOption updateDecorOption(@PathVariable Integer id, @RequestBody DecorOption decorOption) {
        decorOption.setDecorId(id);
        return decorOptionService.saveDecorOption(decorOption);
    }

    @DeleteMapping("/{id}")
    public void deleteDecorOption(@PathVariable Integer id) {
        decorOptionService.deleteDecorOption(id);
    }
}