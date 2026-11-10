package com.hotel.pricing;

import java.math.BigDecimal;

/**
 * One rule that modifies the price of a stay (discount or surcharge).
 * <p>
 * Interface rather than abstract class: rules share no state, they only share a contract.
 * New rules can be added without modifying {@link PriceCalculator} (Open/Closed principle).
 */
public interface PricingRule {

    /**
     * Amount to add to the subtotal: negative for a discount, positive for a surcharge,
     * zero when the rule does not apply. Every rule is computed on the subtotal, so the
     * order of the rules does not change the result.
     */
    BigDecimal adjustment(PricingContext context);

    /** Label shown to the user in the price breakdown. */
    String description();
}
