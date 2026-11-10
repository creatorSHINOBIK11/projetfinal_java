package com.hotel.model;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Deluxe room: breakfast is included and billed per guest on top of the base price.
 */
public class DeluxeRoom extends Room {

    public static final BigDecimal BREAKFAST_PER_GUEST = new BigDecimal("15.00");

    public DeluxeRoom(String number, int floor, BigDecimal basePrice, int capacity, Set<Amenity> amenities) {
        super(number, floor, basePrice, capacity, amenities);
    }

    @Override
    public RoomType getType() {
        return RoomType.DELUXE;
    }

    @Override
    protected BigDecimal priceForGuests(int guestCount) {
        return getBasePrice().add(BREAKFAST_PER_GUEST.multiply(BigDecimal.valueOf(guestCount)));
    }
}
