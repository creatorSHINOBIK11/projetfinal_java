package com.hotel.exception;

/**
 * Thrown when a reservation request or operation breaks a business rule
 * (unknown guest, too many guests, date in the past, forbidden status change...).
 */
public class InvalidReservationException extends HotelException {

    public InvalidReservationException(String message) {
        super(message);
    }
}
