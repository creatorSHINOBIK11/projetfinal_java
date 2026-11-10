package com.hotel.model;

import com.hotel.exception.InvalidReservationException;
import com.hotel.util.Money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A booking of one room by one guest. The reservation protects its own life cycle:
 * CONFIRMED -> CHECKED_IN -> CHECKED_OUT, or CONFIRMED -> CANCELLED.
 */
public class Reservation {

    private final String id;
    private final Guest guest;
    private final Room room;
    private final DateRange stay;
    private final int guestCount;
    private final BigDecimal totalPrice;
    private ReservationStatus status;
    private BigDecimal cancellationFee = Money.ZERO;

    public Reservation(String id, Guest guest, Room room, DateRange stay, int guestCount,
                       BigDecimal totalPrice, ReservationStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L'identifiant de réservation est obligatoire");
        }
        this.id = id;
        this.guest = Objects.requireNonNull(guest, "Le client est obligatoire");
        this.room = Objects.requireNonNull(room, "La chambre est obligatoire");
        this.stay = Objects.requireNonNull(stay, "Les dates sont obligatoires");
        this.status = Objects.requireNonNull(status, "Le statut est obligatoire");
        if (!room.canAccommodate(guestCount)) {
            throw new IllegalArgumentException("La chambre " + room.getNumber() + " ne peut pas accueillir "
                    + guestCount + " personne(s)");
        }
        if (totalPrice == null || totalPrice.signum() < 0) {
            throw new IllegalArgumentException("Le prix total doit être positif");
        }
        this.guestCount = guestCount;
        this.totalPrice = Money.round(totalPrice);
    }

    public boolean isActive() {
        return status.blocksRoom();
    }

    /** True if this reservation prevents the room from being booked for the given period. */
    public boolean conflictsWith(DateRange period) {
        return isActive() && stay.overlaps(period);
    }

    public void checkIn() throws InvalidReservationException {
        requireStatus(ReservationStatus.CONFIRMED, "faire le check-in");
        status = ReservationStatus.CHECKED_IN;
    }

    public void checkOut() throws InvalidReservationException {
        requireStatus(ReservationStatus.CHECKED_IN, "faire le check-out");
        status = ReservationStatus.CHECKED_OUT;
    }

    public void cancel(BigDecimal fee) throws InvalidReservationException {
        requireStatus(ReservationStatus.CONFIRMED, "annuler");
        if (fee == null || fee.signum() < 0 || fee.compareTo(totalPrice) > 0) {
            throw new IllegalArgumentException("Les frais d'annulation doivent être compris entre 0 et le prix total");
        }
        status = ReservationStatus.CANCELLED;
        cancellationFee = Money.round(fee);
    }

    /** Amount actually earned by the hotel for this reservation. */
    public BigDecimal revenue() {
        return status == ReservationStatus.CANCELLED ? cancellationFee : totalPrice;
    }

    private void requireStatus(ReservationStatus expected, String action) throws InvalidReservationException {
        if (status != expected) {
            throw new InvalidReservationException("Impossible de " + action + " pour la réservation " + id
                    + " : statut actuel « " + status.getLabel() + " ».");
        }
    }

    public String getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public DateRange getStay() {
        return stay;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }
}
