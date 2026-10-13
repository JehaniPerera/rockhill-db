package com.rockhill.reception_hall.menu;

public record SelectionLine(
        Integer itemId,        // addonId or decorId
        String name,
        int quantity,
        double chargedAmount,  // unit price actually charged (0.00 when free)
        double lineTotal) {    // chargedAmount x quantity
}
