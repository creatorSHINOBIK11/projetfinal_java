package com.hotel.exception;

import com.hotel.model.DateRange;

/**
 * Thrown when a room is already booked for (part of) the requested period.
 */
public class RoomNotAvailableException extends HotelException {

    public RoomNotAvailableException(String roomNumber, DateRange stay) {
        super("La chambre " + roomNumber + " n'est pas disponible du " + stay + ".");
    }
}
