package com.hotel.service;

import com.hotel.model.DateRange;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Service pour vérifier la disponibilité des chambres sur une période donnée
 * et détecter les conflits de dates.
 */
public class AvailabilityService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public AvailabilityService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = Objects.requireNonNull(roomRepository);
        this.reservationRepository = Objects.requireNonNull(reservationRepository);
    }

    /**
     * Retourne toutes les chambres qui sont libres sur l'intégralité du séjour requestedStay
     * et qui ont une capacité suffisante pour le nombre de personnes.
     */
    public List<Room> findAvailableRooms(DateRange requestedStay, int guestCount) {
        return roomRepository.findAll().stream()
                .filter(room -> room.canAccommodate(guestCount))
                .filter(room -> isRoomAvailable(room.getNumber(), requestedStay))
                .toList();
    }

    /**
     * Vérifie si une chambre précise est libre pendant la période demandée.
     */
    public boolean isRoomAvailable(String roomNumber, DateRange requestedStay) {
        List<Reservation> roomReservations = reservationRepository.findByRoom(roomNumber);
        return roomReservations.stream()
                .noneMatch(res -> res.conflictsWith(requestedStay));
    }

    /**
     * Vérifie si une chambre est occupée à une date précise.
     */
    public boolean isRoomOccupiedOn(String roomNumber, LocalDate date) {
        List<Reservation> roomReservations = reservationRepository.findByRoom(roomNumber);
        return roomReservations.stream()
                .filter(Reservation::isActive)
                .anyMatch(res -> res.getStay().contains(date));
    }
}
