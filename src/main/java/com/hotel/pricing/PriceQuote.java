package com.hotel.pricing;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Result of a price calculation, with the detail of each applied rule.
 */
public record PriceQuote(BigDecimal subtotal, Map<String, BigDecimal> adjustments, BigDecimal total) {
}
