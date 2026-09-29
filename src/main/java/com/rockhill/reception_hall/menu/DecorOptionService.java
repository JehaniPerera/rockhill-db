package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DecorOptionService {

    @Autowired
    private DecorOptionRepository decorOptionRepository;

    public List<DecorOption> getAllDecorOptions() {
        return decorOptionRepository.findAll();
    }

    public Optional<DecorOption> getDecorOptionById(Integer id) {
        return decorOptionRepository.findById(id);
    }

    public DecorOption saveDecorOption(DecorOption decorOption) {
        return decorOptionRepository.save(decorOption);
    }

    public void deleteDecorOption(Integer id) {
        decorOptionRepository.deleteById(id);
    }
}