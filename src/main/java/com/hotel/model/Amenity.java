package com.hotel.model;

public enum Amenity {
    WIFI("Wi-Fi"),
    TV("TV"),
    MINIBAR("Minibar"),
    BALCONY("Balcon"),
    SEA_VIEW("Vue mer"),
    JACUZZI("Jacuzzi");

    private final String label;

    Amenity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
