package com.hotel.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Small helper for money arithmetic.
 * BigDecimal is used instead of double because binary floating point cannot represent
 * amounts such as 0.10 exactly, which leads to rounding errors in prices.
 */
public final class Money {

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private Money() {
    }

    public static BigDecimal of(String amount) {
        return round(new BigDecimal(amount));
    }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentOf(BigDecimal amount, int percent) {
        return round(amount.multiply(BigDecimal.valueOf(percent)).divide(ONE_HUNDRED));
    }

    public static String format(BigDecimal amount) {
        return round(amount).toPlainString() + " EUR";
    }
}
