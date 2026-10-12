package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import static com.rockhill.reception_hall.menu.CatalogValidation.*;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    // Only menus customers may choose (not disabled).
    public List<Menu> getActiveMenus() {
        return menuRepository.findAll().stream().filter(Menu::getActive).toList();
    }

    public Optional<Menu> getMenuById(Integer id) {
        return menuRepository.findById(id);
    }

    public Menu requireActiveMenu(Integer id) {
        Menu menu = menuRepository.findById(id).orElseThrow(() -> bad("Menu not found: " + id));
        if (!menu.getActive()) {
            throw bad("The menu \"" + menu.getName() + "\" is no longer available.");
        }
        return menu;
    }

    public Menu setMenuActive(Integer id, boolean active) {
        Menu menu = menuRepository.findById(id).orElseThrow(() -> notFound("Menu not found: " + id));
        menu.setActive(active);
        return menuRepository.save(menu);
    }

    public Menu saveMenu(Menu menu) {
        menu.setName(requireText(menu.getName(), "Menu name"));
        requireNonNegative(menu.getPricePerGuest(), "Price per guest");

        Integer id = menu.getMenuId();
        if (id == null) {
            menu.setActive(true);                       // new menus start enabled
        } else {
            Menu existing = menuRepository.findById(id)
                    .orElseThrow(() -> notFound("Menu not found: " + id));
            menu.setActive(existing.getActive());       // editing never changes enabled/disabled
        }
        boolean duplicate = menuRepository.findAll().stream()
                .anyMatch(m -> !m.getMenuId().equals(id) && sameName(m.getName(), menu.getName()));
        if (duplicate) {
            throw conflict("A menu named \"" + menu.getName() + "\" already exists.");
        }
        return menuRepository.save(menu);
    }

    public void deleteMenu(Integer id) {
        if (!menuRepository.existsById(id)) {
            throw notFound("Menu not found: " + id);
        }
        try {
            menuRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw conflict("This menu is used by existing bookings and cannot be deleted. Disable it instead so new customers can no longer choose it.");
        }
    }
}
