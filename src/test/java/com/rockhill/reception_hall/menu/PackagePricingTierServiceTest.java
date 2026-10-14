package com.rockhill.reception_hall.menu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackagePricingTierServiceTest {

    @Mock
    private PackagePricingTierRepository tierRepository;

    @Mock
    private PackageRepository packageRepository;

    @InjectMocks
    private PackagePricingTierService service;

    private PackagePricingTier tier(int min, int max, double price) {
        PackagePricingTier t = new PackagePricingTier();
        t.setMinGuests(min);
        t.setMaxGuests(max);
        t.setTotalPrice(price);
        return t;
    }

    private void givenTwoTiers() {
        when(tierRepository.findByPkg_PackageId(1)).thenReturn(List.of(
                tier(100, 150, 530000.0),
                tier(151, 200, 630000.0)));
    }

    @Test
    void guestCountInsideBracketReturnsThatTier() {
        givenTwoTiers();
        Optional<PackagePricingTier> result = service.findTierForGuestCount(1, 120);
        assertTrue(result.isPresent());
        assertEquals(530000.0, result.get().getTotalPrice(), 0.001);
    }

    @Test
    void upperBoundaryBelongsToLowerTier() {
        givenTwoTiers();
        Optional<PackagePricingTier> result = service.findTierForGuestCount(1, 150);
        assertTrue(result.isPresent());
        assertEquals(530000.0, result.get().getTotalPrice(), 0.001);
    }

    @Test
    void firstGuestOfNextBracketReturnsNextTier() {
        givenTwoTiers();
        Optional<PackagePricingTier> result = service.findTierForGuestCount(1, 151);
        assertTrue(result.isPresent());
        assertEquals(630000.0, result.get().getTotalPrice(), 0.001);
    }

    @Test
    void guestCountOutsideAllBracketsReturnsNothing() {
        givenTwoTiers();
        assertTrue(service.findTierForGuestCount(1, 400).isEmpty());
    }
}