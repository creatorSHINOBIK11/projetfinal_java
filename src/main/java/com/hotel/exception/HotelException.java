package com.hotel.exception;

/**
 * Base class of every application-specific exception.
 * It is a checked exception: business errors (room taken, unknown reservation...) are expected
 * situations, so the compiler forces callers to handle them explicitly.
 */
public class HotelException extends Exception {

    public HotelException(String message) {
        super(message);
    }

    public HotelException(String message, Throwable cause) {
        super(message, cause);
    }
}
