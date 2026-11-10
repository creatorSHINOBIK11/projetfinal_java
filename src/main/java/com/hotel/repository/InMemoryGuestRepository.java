package com.hotel.repository;

import com.hotel.model.Guest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryGuestRepository implements GuestRepository {

    private final Map<String, Guest> guests = new LinkedHashMap<>();

    @Override
    public void save(Guest guest) {
        guests.put(guest.getId(), guest);
    }

    @Override
    public Optional<Guest> findById(String id) {
        return Optional.ofNullable(guests.get(id));
    }

    @Override
    public List<Guest> findAll() {
        return new ArrayList<>(guests.values());
    }
}
