package com.hotel.model;

/**
 * Loyalty level of a guest. Each level carries its own discount, so adding a level
 * only requires a new constant here.
 */
public enum LoyaltyLevel {
    STANDARD(0),
    SILVER(5),
    GOLD(10);

    private final int discountPercent;

    LoyaltyLevel(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }
}
