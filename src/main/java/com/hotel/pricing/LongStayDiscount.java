package com.hotel.pricing;

import com.hotel.util.Money;

import java.math.BigDecimal;

/**
 * Discount for long stays, e.g. -10% from 7 nights.
 */
public class LongStayDiscount implements PricingRule {

    private final int minimumNights;
    private final int discountPercent;

    public LongStayDiscount(int minimumNights, int discountPercent) {
        if (minimumNights < 1 || discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Paramètres de remise long séjour invalides");
        }
        this.minimumNights = minimumNights;
        this.discountPercent = discountPercent;
    }

    @Override
    public BigDecimal adjustment(PricingContext context) {
        if (context.stay().nights() < minimumNights) {
            return Money.ZERO;
        }
        return Money.percentOf(context.subtotal(), discountPercent).negate();
    }

    @Override
    public String description() {
        return "Remise long séjour (-" + discountPercent + "% dès " + minimumNights + " nuits)";
    }
}
