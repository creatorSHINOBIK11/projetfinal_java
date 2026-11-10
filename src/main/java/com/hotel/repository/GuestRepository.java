package com.hotel.repository;

import com.hotel.model.Guest;

import java.util.List;
import java.util.Optional;

public interface GuestRepository {

    void save(Guest guest);

    Optional<Guest> findById(String id);

    List<Guest> findAll();
}
