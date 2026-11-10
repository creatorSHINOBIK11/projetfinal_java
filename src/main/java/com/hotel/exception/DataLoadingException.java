package com.hotel.exception;

/**
 * Thrown when a data file cannot be read at all (missing file, missing header...).
 * Invalid individual lines do not raise this exception: they are skipped and reported.
 */
public class DataLoadingException extends HotelException {

    public DataLoadingException(String message) {
        super(message);
    }

    public DataLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
