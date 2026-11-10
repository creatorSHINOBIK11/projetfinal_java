package com.hotel.exception;

/**
 * Thrown when no reservation matches the given identifier.
 */
public class ReservationNotFoundException extends HotelException {

    public ReservationNotFoundException(String reservationId) {
        super("Aucune réservation trouvée avec l'identifiant '" + reservationId + "'.");
    }
}
