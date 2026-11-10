package com.hotel.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A stay period. The check-out day is exclusive: a stay from the 10th to the 12th is 2 nights,
 * and another guest can check in on the 12th.
 */
public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DateRange {
        Objects.requireNonNull(checkIn, "La date d'arrivée est obligatoire");
        Objects.requireNonNull(checkOut, "La date de départ est obligatoire");
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("La date de départ doit être après la date d'arrivée");
        }
    }

    public long nights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** Each night of the stay, identified by the date the guest goes to bed. */
    public Stream<LocalDate> nightDates() {
        return checkIn.datesUntil(checkOut);
    }

    public boolean overlaps(DateRange other) {
        return checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(checkOut);
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(checkIn) && date.isBefore(checkOut);
    }

    @Override
    public String toString() {
        return checkIn.format(DISPLAY_FORMAT) + " au " + checkOut.format(DISPLAY_FORMAT);
    }
}
