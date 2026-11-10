package com.hotel.service;

import com.hotel.model.DateRange;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.util.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Service générant des rapports de gestion :
 * chiffre d'affaires, taux d'occupation sur une date ou période.
 */
public class ReportService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public ReportService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = Objects.requireNonNull(roomRepository);
        this.reservationRepository = Objects.requireNonNull(reservationRepository);
    }

    /**
     * Calcule le chiffre d'affaires total encaissé (réservations effectives + frais d'annulations).
     */
    public BigDecimal calculateTotalRevenue() {
        return reservationRepository.findAll().stream()
                .map(Reservation::revenue)
                .reduce(Money.ZERO, BigDecimal::add);
    }

    /**
     * Taux d'occupation en pourcentage pour une date donnée (entre 0.0 et 100.0).
     */
    public double calculateOccupancyRateOn(LocalDate date) {
        List<Room> allRooms = roomRepository.findAll();
        if (allRooms.isEmpty()) {
            return 0.0;
        }

        long occupiedRoomsCount = reservationRepository.findAll().stream()
                .filter(Reservation::isActive)
                .filter(res -> res.getStay().contains(date))
                .map(res -> res.getRoom().getNumber())
                .distinct()
                .count();

        return (occupiedRoomsCount * 100.0) / allRooms.size();
    }

    /**
     * Taux d'occupation moyen sur une plage de dates.
     */
    public double calculateAverageOccupancy(DateRange period) {
        List<LocalDate> days = period.nightDates().toList();
        if (days.isEmpty()) {
            return 0.0;
        }
        double sum = days.stream()
                .mapToDouble(this::calculateOccupancyRateOn)
                .sum();
        return sum / days.size();
    }
}
