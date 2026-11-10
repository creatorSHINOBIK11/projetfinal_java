package com.hotel.pricing;

import com.hotel.model.LoyaltyLevel;
import com.hotel.util.Money;

import java.math.BigDecimal;

/**
 * Discount depending on the guest's loyalty level (the percentage is defined in {@link LoyaltyLevel}).
 */
public class LoyaltyDiscount implements PricingRule {

    @Override
    public BigDecimal adjustment(PricingContext context) {
        int percent = context.guest().getLoyaltyLevel().getDiscountPercent();
        return Money.percentOf(context.subtotal(), percent).negate();
    }

    @Override
    public String description() {
        return "Remise fidélité";
    }
}
