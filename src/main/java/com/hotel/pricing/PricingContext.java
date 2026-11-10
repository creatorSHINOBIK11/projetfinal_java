package com.hotel.pricing;

import com.hotel.model.DateRange;
import com.hotel.model.Guest;
import com.hotel.model.Room;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Everything a pricing rule may need to know about a stay.
 */
public record PricingContext(Room room, Guest guest, DateRange stay, int guestCount) {

    public PricingContext {
        Objects.requireNonNull(room);
        Objects.requireNonNull(guest);
        Objects.requireNonNull(stay);
    }

    public BigDecimal nightlyPrice() {
        return room.calculateNightlyPrice(guestCount);
    }

    /** Price of the stay before any rule is applied. */
    public BigDecimal subtotal() {
        return nightlyPrice().multiply(BigDecimal.valueOf(stay.nights()));
    }
}
