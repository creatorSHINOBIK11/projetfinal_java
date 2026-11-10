package com.hotel.model;

public enum ReservationStatus {
    CONFIRMED("Confirmée", true),
    CHECKED_IN("En cours", true),
    CHECKED_OUT("Terminée", false),
    CANCELLED("Annulée", false);

    private final String label;
    /** True when a reservation in this status prevents other bookings of the same room. */
    private final boolean blocksRoom;

    ReservationStatus(String label, boolean blocksRoom) {
        this.label = label;
        this.blocksRoom = blocksRoom;
    }

    public String getLabel() {
        return label;
    }

    public boolean blocksRoom() {
        return blocksRoom;
    }
}
