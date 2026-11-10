package com.hotel.pricing;

import com.hotel.util.Money;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes the price of a stay by applying a list of {@link PricingRule}s.
 * The calculator does not know any concrete rule: they are injected at construction.
 */
public class PriceCalculator {

    private final List<PricingRule> rules;

    public PriceCalculator(List<PricingRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public PriceQuote quote(PricingContext context) {
        BigDecimal subtotal = Money.round(context.subtotal());
        Map<String, BigDecimal> adjustments = new LinkedHashMap<>();
        for (PricingRule rule : rules) {
            BigDecimal adjustment = rule.adjustment(context);
            if (adjustment.signum() != 0) {
                adjustments.put(rule.description(), Money.round(adjustment));
            }
        }
        BigDecimal total = adjustments.values().stream()
                .reduce(subtotal, BigDecimal::add)
                .max(Money.ZERO);
        return new PriceQuote(subtotal, Collections.unmodifiableMap(adjustments), Money.round(total));
    }
}
