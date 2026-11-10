package com.hotel.repository;

import com.hotel.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {

    void save(Room room);

    Optional<Room> findByNumber(String number);

    List<Room> findAll();
}
