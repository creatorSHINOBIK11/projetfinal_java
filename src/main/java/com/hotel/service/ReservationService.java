package com.hotel.service;

import com.hotel.exception.InvalidReservationException;
import com.hotel.exception.ReservationNotFoundException;
import com.hotel.exception.RoomNotAvailableException;
import com.hotel.model.DateRange;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.model.Room;
import com.hotel.pricing.PriceCalculator;
import com.hotel.pricing.PriceQuote;
import com.hotel.pricing.PricingContext;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Service gérant le cycle de vie des réservations :
 * création, annulation, check-in, check-out.
 */
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final AvailabilityService availabilityService;
    private final PriceCalculator priceCalculator;
    private final CancellationPolicy cancellationPolicy;
    private final Clock clock; // Injecté pour faciliter les tests temporels déterministes

    public ReservationService(ReservationRepository reservationRepository,
                              RoomRepository roomRepository,
                              GuestRepository guestRepository,
                              AvailabilityService availabilityService,
                              PriceCalculator priceCalculator,
                              CancellationPolicy cancellationPolicy,
                              Clock clock) {
        this.reservationRepository = Objects.requireNonNull(reservationRepository);
        this.roomRepository = Objects.requireNonNull(roomRepository);
        this.guestRepository = Objects.requireNonNull(guestRepository);
        this.availabilityService = Objects.requireNonNull(availabilityService);
        this.priceCalculator = Objects.requireNonNull(priceCalculator);
        this.cancellationPolicy = Objects.requireNonNull(cancellationPolicy);
        this.clock = Objects.requireNonNull(clock);
    }

    public PriceQuote calculateQuote(String guestId, String roomNumber, DateRange stay, int guestCount)
            throws InvalidReservationException {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new InvalidReservationException("Client introuvable : " + guestId));
        Room room = roomRepository.findByNumber(roomNumber)
                .orElseThrow(() -> new InvalidReservationException("Chambre introuvable : " + roomNumber));

        if (!room.canAccommodate(guestCount)) {
            throw new InvalidReservationException("La chambre " + roomNumber + " ne peut accueillir " + guestCount + " personne(s)");
        }

        PricingContext context = new PricingContext(room, guest, stay, guestCount);
        return priceCalculator.quote(context);
    }

    public Reservation createReservation(String guestId, String roomNumber, DateRange stay, int guestCount)
            throws RoomNotAvailableException, InvalidReservationException {
        LocalDate today = LocalDate.now(clock);
        if (stay.checkIn().isBefore(today)) {
            throw new InvalidReservationException("La date d'arrivée (" + stay.checkIn() + ") ne peut pas être dans le passé.");
        }

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new InvalidReservationException("Client introuvable avec l'ID : " + guestId));
        Room room = roomRepository.findByNumber(roomNumber)
                .orElseThrow(() -> new InvalidReservationException("Chambre introuvable avec le numéro : " + roomNumber));

        if (!room.canAccommodate(guestCount)) {
            throw new InvalidReservationException("La chambre " + roomNumber + " ne peut accueillir " + guestCount + " personne(s) (capacité : " + room.getCapacity() + ").");
        }

        if (!availabilityService.isRoomAvailable(roomNumber, stay)) {
            throw new RoomNotAvailableException(roomNumber, stay);
        }

        PriceQuote quote = calculateQuote(guestId, roomNumber, stay, guestCount);
        String reservationId = reservationRepository.nextId();

        Reservation reservation = new Reservation(
                reservationId,
                guest,
                room,
                stay,
                guestCount,
                quote.total(),
                ReservationStatus.CONFIRMED
        );

        reservationRepository.save(reservation);
        return reservation;
    }

    public BigDecimal cancelReservation(String reservationId)
            throws ReservationNotFoundException, InvalidReservationException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        LocalDate today = LocalDate.now(clock);
        BigDecimal fee = cancellationPolicy.calculateFee(reservation, today);
        reservation.cancel(fee);
        reservationRepository.save(reservation);
        return fee;
    }

    public void checkIn(String reservationId)
            throws ReservationNotFoundException, InvalidReservationException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        LocalDate today = LocalDate.now(clock);
        if (today.isBefore(reservation.getStay().checkIn())) {
            throw new InvalidReservationException("Check-in impossible avant le jour d'arrivée (" + reservation.getStay().checkIn() + ").");
        }
        reservation.checkIn();
        reservationRepository.save(reservation);
    }

    public void checkOut(String reservationId)
            throws ReservationNotFoundException, InvalidReservationException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        reservation.checkOut();
        reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservation(String id) throws ReservationNotFoundException {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }
}
