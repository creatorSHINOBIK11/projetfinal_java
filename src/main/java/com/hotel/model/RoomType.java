package com.hotel.model;

public enum RoomType {
    STANDARD("Standard"),
    DELUXE("Deluxe"),
    SUITE("Suite");

    private final String label;

    RoomType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
