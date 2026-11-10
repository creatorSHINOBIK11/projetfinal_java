package com.hotel.model;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Suite: the base price covers two guests, each extra guest pays a fixed fee per night.
 */
public class Suite extends Room {

    public static final int INCLUDED_GUESTS = 2;
    public static final BigDecimal EXTRA_GUEST_FEE = new BigDecimal("40.00");

    public Suite(String number, int floor, BigDecimal basePrice, int capacity, Set<Amenity> amenities) {
        super(number, floor, basePrice, capacity, amenities);
    }

    @Override
    public RoomType getType() {
        return RoomType.SUITE;
    }

    @Override
    protected BigDecimal priceForGuests(int guestCount) {
        int extraGuests = Math.max(0, guestCount - INCLUDED_GUESTS);
        return getBasePrice().add(EXTRA_GUEST_FEE.multiply(BigDecimal.valueOf(extraGuests)));
    }
}
