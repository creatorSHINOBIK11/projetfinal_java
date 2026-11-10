package com.hotel.model;

import com.hotel.util.Money;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Common state and behaviour of every room.
 * <p>
 * Abstract because a "generic" room does not exist in the hotel: every room has a concrete
 * type, and each type computes its nightly price differently ({@link #priceForGuests(int)}).
 * An abstract class (rather than an interface) is used because all rooms share fields and
 * validation logic.
 */
public abstract class Room {

    private final String number;
    private final int floor;
    private final BigDecimal basePrice;
    private final int capacity;
    private final Set<Amenity> amenities;

    protected Room(String number, int floor, BigDecimal basePrice, int capacity, Set<Amenity> amenities) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Le numéro de chambre est obligatoire");
        }
        if (floor < 0) {
            throw new IllegalArgumentException("L'étage ne peut pas être négatif");
        }
        if (basePrice == null || basePrice.signum() <= 0) {
            throw new IllegalArgumentException("Le prix de base doit être strictement positif");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("La capacité doit être d'au moins 1 personne");
        }
        this.number = number;
        this.floor = floor;
        this.basePrice = Money.round(basePrice);
        this.capacity = capacity;
        this.amenities = amenities == null || amenities.isEmpty()
                ? EnumSet.noneOf(Amenity.class)
                : EnumSet.copyOf(amenities);
    }

    public abstract RoomType getType();

    /** Type-specific price for one night, before any discount or surcharge. */
    protected abstract BigDecimal priceForGuests(int guestCount);

    /**
     * Price of one night for the given number of guests.
     * Final so that every subclass goes through the same capacity check (Liskov: a subclass
     * cannot weaken this contract).
     */
    public final BigDecimal calculateNightlyPrice(int guestCount) {
        if (!canAccommodate(guestCount)) {
            throw new IllegalArgumentException("La chambre " + number + " accepte de 1 à " + capacity + " personne(s)");
        }
        return Money.round(priceForGuests(guestCount));
    }

    public boolean canAccommodate(int guestCount) {
        return guestCount >= 1 && guestCount <= capacity;
    }

    public boolean hasAmenity(Amenity amenity) {
        return amenities.contains(amenity);
    }

    public String getNumber() {
        return number;
    }

    public int getFloor() {
        return floor;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public int getCapacity() {
        return capacity;
    }

    public Set<Amenity> getAmenities() {
        return Collections.unmodifiableSet(amenities);
    }

    @Override
    public String toString() {
        return getType().getLabel() + " " + number;
    }
}
