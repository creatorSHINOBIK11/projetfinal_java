package com.hotel.repository;

import com.hotel.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    void save(Reservation reservation);

    Optional<Reservation> findById(String id);

    List<Reservation> findAll();

    List<Reservation> findByRoom(String roomNumber);

    List<Reservation> findByGuest(String guestId);

    /** Generates the identifier of the next reservation (R001, R002...). */
    String nextId();
}
