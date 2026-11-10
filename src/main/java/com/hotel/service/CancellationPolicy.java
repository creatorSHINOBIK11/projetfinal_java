package com.hotel.service;

import com.hotel.model.DateRange;
import com.hotel.model.Reservation;
import com.hotel.util.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Règle d'annulation :
 * - Si l'annulation a lieu au moins 2 jours (48h) avant la date d'arrivée : gratuite (0 €).
 * - Sinon : 50% du montant total de la réservation en frais d'annulation.
 */
public class CancellationPolicy {

    private final int freeCancellationDays;
    private final int lateCancellationFeePercent;

    public CancellationPolicy() {
        this(2, 50);
    }

    public CancellationPolicy(int freeCancellationDays, int lateCancellationFeePercent) {
        this.freeCancellationDays = freeCancellationDays;
        this.lateCancellationFeePercent = lateCancellationFeePercent;
    }

    public BigDecimal calculateFee(Reservation reservation, LocalDate cancellationDate) {
        Objects.requireNonNull(reservation);
        Objects.requireNonNull(cancellationDate);

        DateRange stay = reservation.getStay();
        long daysUntilArrival = ChronoUnit.DAYS.between(cancellationDate, stay.checkIn());

        if (daysUntilArrival >= freeCancellationDays) {
            return Money.ZERO;
        } else {
            return Money.percentOf(reservation.getTotalPrice(), lateCancellationFeePercent);
        }
    }

    public int getFreeCancellationDays() {
        return freeCancellationDays;
    }

    public int getLateCancellationFeePercent() {
        return lateCancellationFeePercent;
    }
}
