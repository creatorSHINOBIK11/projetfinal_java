package com.hotel.model;

import java.util.Objects;

public class Guest {

    private final String id;
    private final String fullName;
    private final String email;
    private final LoyaltyLevel loyaltyLevel;

    public Guest(String id, String fullName, String email, LoyaltyLevel loyaltyLevel) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L'identifiant du client est obligatoire");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Le nom du client est obligatoire");
        }
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Email invalide : " + email);
        }
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.loyaltyLevel = Objects.requireNonNull(loyaltyLevel, "Le niveau de fidélité est obligatoire");
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public LoyaltyLevel getLoyaltyLevel() {
        return loyaltyLevel;
    }

    @Override
    public String toString() {
        return fullName + " (" + id + ")";
    }
}
