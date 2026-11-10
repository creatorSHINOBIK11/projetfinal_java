package com.hotel.pricing;

import com.hotel.model.DateRange;
import com.hotel.model.Guest;
import com.hotel.model.LoyaltyLevel;
import com.hotel.model.StandardRoom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour le moteur de tarification")
class PriceCalculatorTest {

    @Test
    @DisplayName("Devrait appliquer la remise fidélité correctement")
    void shouldApplyLoyaltyDiscount() {
        Guest guestGold = new Guest("G001", "Claire", "claire@test.com", LoyaltyLevel.GOLD); // 10%
        StandardRoom room = new StandardRoom("101", 1, new BigDecimal("100.00"), 2, Set.of());
        DateRange stay = new DateRange(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12)); // 2 nuits = 200€

        PricingContext ctx = new PricingContext(room, guestGold, stay, 1);
        PriceCalculator calc = new PriceCalculator(List.of(new LoyaltyDiscount()));

        PriceQuote quote = calc.quote(ctx);
        assertEquals(new BigDecimal("200.00"), quote.subtotal());
        assertEquals(new BigDecimal("-20.00"), quote.adjustments().get("Remise fidélité"));
        assertEquals(new BigDecimal("180.00"), quote.total());
    }

    @Test
    @DisplayName("Devrait cumuler remise fidélité et remise long séjour")
    void shouldCombineLongStayAndLoyaltyDiscounts() {
        Guest guestSilver = new Guest("G002", "Bob", "bob@test.com", LoyaltyLevel.SILVER); // 5%
        StandardRoom room = new StandardRoom("101", 1, new BigDecimal("100.00"), 2, Set.of());
        // Séjour de 10 nuits (>= 7 nuits, remise 10%)
        DateRange stay = new DateRange(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 11)); // 10 nuits = 1000€

        PricingContext ctx = new PricingContext(room, guestSilver, stay, 1);
        PriceCalculator calc = new PriceCalculator(List.of(
                new LoyaltyDiscount(),
                new LongStayDiscount(7, 10)
        ));

        PriceQuote quote = calc.quote(ctx);
        // Sous-total = 1000
        // Remise fidélité 5% = -50
        // Remise long séjour 10% = -100
        // Total = 850
        assertEquals(new BigDecimal("1000.00"), quote.subtotal());
        assertEquals(new BigDecimal("850.00"), quote.total());
    }

    @Test
    @DisplayName("Devrait appliquer le supplément haute saison uniquement sur les nuits concernées")
    void shouldApplyHighSeasonSurchargeOnlyOnSummerNights() {
        Guest guest = new Guest("G003", "Alice", "alice@test.com", LoyaltyLevel.STANDARD);
        StandardRoom room = new StandardRoom("101", 1, new BigDecimal("100.00"), 2, Set.of());

        // Du 30 Juin au 3 Juillet (3 nuits : 30 Juin [non été], 1er Juillet [été], 2 Juillet [été])
        DateRange stay = new DateRange(LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 3));
        PricingContext ctx = new PricingContext(room, guest, stay, 1);

        PriceCalculator calc = new PriceCalculator(List.of(
                new HighSeasonSurcharge(Set.of(Month.JULY, Month.AUGUST), 20) // +20%
        ));

        PriceQuote quote = calc.quote(ctx);
        // Sous-total = 3 nuits * 100 = 300
        // 2 nuits en Juillet = 200 * 20% = +40
        // Total = 340
        assertEquals(new BigDecimal("300.00"), quote.subtotal());
        assertEquals(new BigDecimal("40.00"), quote.adjustments().get("Supplément haute saison (+20%)"));
        assertEquals(new BigDecimal("340.00"), quote.total());
    }
}
