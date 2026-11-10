package com.hotel.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour DateRange")
class DateRangeTest {

    @Test
    @DisplayName("Devrait calculer correctement le nombre de nuits")
    void shouldCalculateNightsCorrectly() {
        DateRange stay = new DateRange(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15));
        assertEquals(5, stay.nights());
    }

    @Test
    @DisplayName("Devrait refuser une date de départ égale ou antérieure à l'arrivée")
    void shouldThrowExceptionWhenCheckOutBeforeOrSameAsCheckIn() {
        LocalDate now = LocalDate.of(2026, 11, 10);
        assertThrows(IllegalArgumentException.class, () -> new DateRange(now, now));
        assertThrows(IllegalArgumentException.class, () -> new DateRange(now, now.minusDays(1)));
    }

    @Test
    @DisplayName("Devrait détecter les chevauchements de dates")
    void shouldDetectOverlappingRanges() {
        DateRange stay1 = new DateRange(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15));

        // Période qui chevauche au milieu
        DateRange overlapMiddle = new DateRange(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 14));
        assertTrue(stay1.overlaps(overlapMiddle));

        // Période qui englobe complètement
        DateRange overlapWider = new DateRange(LocalDate.of(2026, 11, 8), LocalDate.of(2026, 11, 18));
        assertTrue(stay1.overlaps(overlapWider));

        // Période contiguë (départ le 15, arrivée du suivant le 15) : NE doit PAS se chevaucher
        DateRange contiguous = new DateRange(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));
        assertFalse(stay1.overlaps(contiguous));

        // Période antérieure contiguë
        DateRange contiguousBefore = new DateRange(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 10));
        assertFalse(stay1.overlaps(contiguousBefore));
    }
}
