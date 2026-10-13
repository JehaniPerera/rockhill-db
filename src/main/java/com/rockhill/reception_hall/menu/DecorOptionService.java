package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import static com.rockhill.reception_hall.menu.CatalogValidation.*;

@Service
public class DecorOptionService {

    @Autowired
    private DecorOptionRepository decorOptionRepository;

    public List<DecorOption> getAllDecorOptions() {
        return decorOptionRepository.findAll();
    }

    public List<DecorOption> getActiveDecorOptions() {
        return decorOptionRepository.findAll().stream().filter(DecorOption::getActive).toList();
    }

    public Optional<DecorOption> getDecorOptionById(Integer id) {
        return decorOptionRepository.findById(id);
    }

    public DecorOption setDecorOptionActive(Integer id, boolean active) {
        DecorOption decor = decorOptionRepository.findById(id)
                .orElseThrow(() -> notFound("Decor option not found: " + id));
        decor.setActive(active);
        return decorOptionRepository.save(decor);
    }

    public DecorOption saveDecorOption(DecorOption decor) {
        decor.setCategory(requireText(decor.getCategory(), "Category"));
        decor.setName(requireText(decor.getName(), "Decor name"));
        requireNonNegative(decor.getPrice(), "Price");
        if (decor.getIsSingular() == null) decor.setIsSingular(false);

        Integer id = decor.getDecorId();
        if (id == null) {
            decor.setActive(true);
        } else {
            DecorOption existing = decorOptionRepository.findById(id)
                    .orElseThrow(() -> notFound("Decor option not found: " + id));
            decor.setActive(existing.getActive());
        }
        boolean duplicate = decorOptionRepository.findAll().stream()
                .anyMatch(d -> !d.getDecorId().equals(id)
                        && sameName(d.getCategory(), decor.getCategory())
                        && sameName(d.getName(), decor.getName()));
        if (duplicate) {
            throw conflict("\"" + decor.getName() + "\" already exists in the category \"" + decor.getCategory() + "\".");
        }
        return decorOptionRepository.save(decor);
    }

    public void deleteDecorOption(Integer id) {
        if (!decorOptionRepository.existsById(id)) {
            throw notFound("Decor option not found: " + id);
        }
        try {
            decorOptionRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw conflict("This decor option is used by existing bookings and cannot be deleted. Disable it instead so new customers can no longer choose it.");
        }
    }
}
