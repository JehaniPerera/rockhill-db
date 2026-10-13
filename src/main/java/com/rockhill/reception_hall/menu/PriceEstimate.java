package com.rockhill.reception_hall.menu;

public record PriceEstimate(
        String eventType,
        int guestCount,
        double basePrice,        // wedding: flat tier total | other: menu rate x guests
        double lowGuestSurcharge,// other events only: Rs. 35,000 when guests < 150
        double addOnTotal,       // sum of chargedAmount x quantity
        double decorTotal,       // sum of chargedAmount x quantity (0 for weddings)
        double total) {
}
