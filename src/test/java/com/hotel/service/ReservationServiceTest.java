package com.hotel.service;

import com.hotel.exception.InvalidReservationException;
import com.hotel.exception.ReservationNotFoundException;
import com.hotel.exception.RoomNotAvailableException;
import com.hotel.model.DateRange;
import com.hotel.model.Guest;
import com.hotel.model.LoyaltyLevel;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.model.Room;
import com.hotel.model.StandardRoom;
import com.hotel.pricing.PriceCalculator;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.InMemoryGuestRepository;
import com.hotel.repository.InMemoryReservationRepository;
import com.hotel.repository.InMemoryRoomRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour ReservationService")
class ReservationServiceTest {

    private RoomRepository roomRepository;
    private GuestRepository guestRepository;
    private ReservationRepository reservationRepository;
    private ReservationService reservationService;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        roomRepository = new InMemoryRoomRepository();
        guestRepository = new InMemoryGuestRepository();
        reservationRepository = new InMemoryReservationRepository();

        // Date fixe : 10 Novembre 2026
        fixedClock = Clock.fixed(Instant.parse("2026-11-10T10:00:00Z"), ZoneId.of("UTC"));

        AvailabilityService availabilityService = new AvailabilityService(roomRepository, reservationRepository);
        PriceCalculator priceCalculator = new PriceCalculator(List.of()); // Pas de règle pour simplifier
        CancellationPolicy cancellationPolicy = new CancellationPolicy(2, 50);

        reservationService = new ReservationService(
                reservationRepository,
                roomRepository,
                guestRepository,
                availabilityService,
                priceCalculator,
                cancellationPolicy,
                fixedClock
        );

        // Données de test
        roomRepository.save(new StandardRoom("101", 1, new BigDecimal("100.00"), 2, Set.of()));
        guestRepository.save(new Guest("G001", "Alice", "alice@test.com", LoyaltyLevel.STANDARD));
    }

    @Test
    @DisplayName("Devrait créer une réservation valide")
    void shouldCreateReservationSuccessfully() throws Exception {
        DateRange stay = new DateRange(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));
        Reservation res = reservationService.createReservation("G001", "101", stay, 2);

        assertNotNull(res.getId());
        assertEquals(ReservationStatus.CONFIRMED, res.getStatus());
        assertEquals(new BigDecimal("300.00"), res.getTotalPrice());
    }

    @Test
    @DisplayName("Devrait refuser une réservation dont la date est dans le passé")
    void shouldRejectReservationInThePast() {
        // Date antérieure au 10 Novembre 2026
        DateRange pastStay = new DateRange(LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 8));

        assertThrows(InvalidReservationException.class, () ->
                reservationService.createReservation("G001", "101", pastStay, 1));
    }

    @Test
    @DisplayName("Devrait lever RoomNotAvailableException en cas de conflit de dates")
    void shouldThrowExceptionWhenRoomAlreadyBooked() throws Exception {
        DateRange stay1 = new DateRange(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));
        reservationService.createReservation("G001", "101", stay1, 1);

        // Deuxième réservation qui chevauche la première
        DateRange conflictStay = new DateRange(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 19));
        assertThrows(RoomNotAvailableException.class, () ->
                reservationService.createReservation("G001", "101", conflictStay, 1));
    }

    @Test
    @DisplayName("Devrait appliquer 0€ de frais si annulation > 48h avant l'arrivée")
    void shouldCancelForFreeWhenMoreThan48HoursBefore() throws Exception {
        // Arrivée le 15, aujourd'hui c'est le 10 (5 jours avant)
        DateRange stay = new DateRange(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 18));
        Reservation res = reservationService.createReservation("G001", "101", stay, 1);

        BigDecimal fee = reservationService.cancelReservation(res.getId());
        assertEquals(new BigDecimal("0.00"), fee);
        assertEquals(ReservationStatus.CANCELLED, res.getStatus());
    }

    @Test
    @DisplayName("Devrait appliquer 50% de frais si annulation tardive (<= 48h)")
    void shouldChargeCancellationFeeWhenLateCancellation() throws Exception {
        // Arrivée le 11, aujourd'hui c'est le 10 (1 jour avant)
        DateRange stay = new DateRange(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 13)); // 2 nuits = 200€
        Reservation res = reservationService.createReservation("G001", "101", stay, 1);

        BigDecimal fee = reservationService.cancelReservation(res.getId());
        assertEquals(new BigDecimal("100.00"), fee); // 50% de 200€
        assertEquals(ReservationStatus.CANCELLED, res.getStatus());
    }

    @Test
    @DisplayName("Devrait lever ReservationNotFoundException si l'identifiant n'existe pas")
    void shouldThrowWhenReservationNotFound() {
        assertThrows(ReservationNotFoundException.class, () ->
                reservationService.cancelReservation("UNKNOWN_ID"));
    }
}
