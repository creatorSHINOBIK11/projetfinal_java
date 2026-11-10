package com.hotel.repository;

import com.hotel.model.Room;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryRoomRepository implements RoomRepository {

    // LinkedHashMap: O(1) lookup by number while keeping the file order for display
    private final Map<String, Room> rooms = new LinkedHashMap<>();

    @Override
    public void save(Room room) {
        rooms.put(room.getNumber(), room);
    }

    @Override
    public Optional<Room> findByNumber(String number) {
        return Optional.ofNullable(rooms.get(number));
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }
}
