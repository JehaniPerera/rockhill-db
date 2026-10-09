package com.rockhill.reception_hall.menu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AddOnService {

    @Autowired
    private AddOnRepository addOnRepository;

    public List<AddOn> getAllAddOns() {
        return addOnRepository.findAll();
    }

    public List<AddOn> getAddOnsForEventType(String eventType) {
        if ("Wedding".equals(eventType)) {
            return addOnRepository.findAll();
        }
        return addOnRepository.findAll().stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsWeddingExclusive()))
                .toList();
    }

    public Optional<AddOn> getAddOnById(Integer id) {
        return addOnRepository.findById(id);
    }

    public AddOn saveAddOn(AddOn addOn) {
        return addOnRepository.save(addOn);
    }

    public void deleteAddOn(Integer id) {
        addOnRepository.deleteById(id);
    }
}