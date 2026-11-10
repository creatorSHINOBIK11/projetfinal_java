package com.hotel.pricing;

import com.hotel.util.Money;

import java.math.BigDecimal;
import java.time.Month;
import java.util.EnumSet;
import java.util.Set;

/**
 * Surcharge applied only to the nights that fall in the high season months.
 * A stay from 30 June to 3 July pays the surcharge on 2 nights out of 3.
 */
public class HighSeasonSurcharge implements PricingRule {

    private final Set<Month> highSeasonMonths;
    private final int surchargePercent;

    public HighSeasonSurcharge(Set<Month> highSeasonMonths, int surchargePercent) {
        if (highSeasonMonths == null || highSeasonMonths.isEmpty() || surchargePercent < 0) {
            throw new IllegalArgumentException("Paramètres de haute saison invalides");
        }
        this.highSeasonMonths = EnumSet.copyOf(highSeasonMonths);
        this.surchargePercent = surchargePercent;
    }

    @Override
    public BigDecimal adjustment(PricingContext context) {
        long highSeasonNights = context.stay().nightDates()
                .filter(date -> highSeasonMonths.contains(date.getMonth()))
                .count();
        BigDecimal highSeasonAmount = context.nightlyPrice().multiply(BigDecimal.valueOf(highSeasonNights));
        return Money.percentOf(highSeasonAmount, surchargePercent);
    }

    @Override
    public String description() {
        return "Supplément haute saison (+" + surchargePercent + "%)";
    }
}
