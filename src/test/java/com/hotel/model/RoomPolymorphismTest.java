package com.hotel.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour le polymorphisme des chambres")
class RoomPolymorphismTest {

    @Test
    @DisplayName("StandardRoom : le prix par nuit reste fixe quel que soit le nombre d'occupants")
    void standardRoomPriceShouldBeFixed() {
        Room room = new StandardRoom("101", 1, new BigDecimal("100.00"), 2, Set.of(Amenity.WIFI));
        assertEquals(new BigDecimal("100.00"), room.calculateNightlyPrice(1));
        assertEquals(new BigDecimal("100.00"), room.calculateNightlyPrice(2));
    }

    @Test
    @DisplayName("DeluxeRoom : ajoute le petit-déjeuner par personne au prix de base")
    void deluxeRoomPriceShouldIncludeBreakfastPerGuest() {
        Room room = new DeluxeRoom("201", 2, new BigDecimal("150.00"), 3, Set.of(Amenity.WIFI, Amenity.BALCONY));
        // 1 personne = 150 + 15 = 165
        assertEquals(new BigDecimal("165.00"), room.calculateNightlyPrice(1));
        // 2 personnes = 150 + 30 = 180
        assertEquals(new BigDecimal("180.00"), room.calculateNightlyPrice(2));
    }

    @Test
    @DisplayName("Suite : 2 personnes incluses, supplément pour chaque personne supplémentaire")
    void suitePriceShouldChargeExtraForGuestsAboveTwo() {
        Room room = new Suite("301", 3, new BigDecimal("250.00"), 4, Set.of(Amenity.JACUZZI));
        // 1 ou 2 personnes = 250
        assertEquals(new BigDecimal("250.00"), room.calculateNightlyPrice(1));
        assertEquals(new BigDecimal("250.00"), room.calculateNightlyPrice(2));
        // 3 personnes = 250 + 40 = 290
        assertEquals(new BigDecimal("290.00"), room.calculateNightlyPrice(3));
        // 4 personnes = 250 + 80 = 330
        assertEquals(new BigDecimal("330.00"), room.calculateNightlyPrice(4));
    }

    @Test
    @DisplayName("Devrait lever une exception si le nombre d'invités dépasse la capacité ou est nul")
    void shouldThrowExceptionWhenGuestCountExceedsCapacity() {
        Room room = new StandardRoom("101", 1, new BigDecimal("80.00"), 2, Set.of());
        assertThrows(IllegalArgumentException.class, () -> room.calculateNightlyPrice(0));
        assertThrows(IllegalArgumentException.class, () -> room.calculateNightlyPrice(3));
    }
}
