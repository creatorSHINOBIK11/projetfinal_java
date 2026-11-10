package com.hotel.repository;

import com.hotel.model.Reservation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryReservationRepository implements ReservationRepository {

    private static final String ID_PREFIX = "R";

    private final Map<String, Reservation> reservations = new LinkedHashMap<>();

    @Override
    public void save(Reservation reservation) {
        reservations.put(reservation.getId(), reservation);
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    @Override
    public List<Reservation> findByRoom(String roomNumber) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getRoom().getNumber().equals(roomNumber))
                .toList();
    }

    @Override
    public List<Reservation> findByGuest(String guestId) {
        return reservations.values().stream()
                .filter(reservation -> reservation.getGuest().getId().equals(guestId))
                .toList();
    }

    @Override
    public String nextId() {
        // Based on the highest existing number so that ids loaded from the file are never reused
        int highest = reservations.keySet().stream()
                .filter(id -> id.matches(ID_PREFIX + "\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(ID_PREFIX.length())))
                .max()
                .orElse(0);
        return String.format("%s%03d", ID_PREFIX, highest + 1);
    }
}
