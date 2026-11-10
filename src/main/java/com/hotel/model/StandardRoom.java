package com.hotel.model;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Basic room: the nightly price is the base price, whatever the number of guests.
 */
public class StandardRoom extends Room {

    public StandardRoom(String number, int floor, BigDecimal basePrice, int capacity, Set<Amenity> amenities) {
        super(number, floor, basePrice, capacity, amenities);
    }

    @Override
    public RoomType getType() {
        return RoomType.STANDARD;
    }

    @Override
    protected BigDecimal priceForGuests(int guestCount) {
        return getBasePrice();
    }
}
