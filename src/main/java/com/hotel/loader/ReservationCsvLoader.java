package com.hotel.loader;

import com.hotel.exception.DataLoadingException;
import com.hotel.model.DateRange;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.model.Room;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Charge les réservations existantes :
 * format attendu : id,guestId,roomNumber,checkIn,checkOut,guestCount,totalPrice,status
 */
public class ReservationCsvLoader {

    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;

    public ReservationCsvLoader(ReservationRepository reservationRepository,
                                GuestRepository guestRepository,
                                RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
    }

    public int load(Path path) throws DataLoadingException {
        List<String[]> rows = CsvReader.read(path);
        return parseRows(rows);
    }

    public int loadFromClasspath(String resourcePath) throws DataLoadingException {
        List<String[]> rows = CsvReader.readFromClasspath(resourcePath);
        return parseRows(rows);
    }

    private int parseRows(List<String[]> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        int startIndex = 0;
        if (rows.get(0)[0].equalsIgnoreCase("id")) {
            startIndex = 1;
        }

        int count = 0;
        for (int i = startIndex; i < rows.size(); i++) {
            String[] r = rows.get(i);
            if (r.length < 8) {
                System.err.println("Ligne réservation invalide : " + Arrays.toString(r));
                continue;
            }
            try {
                String id = r[0];
                String guestId = r[1];
                String roomNumber = r[2];
                LocalDate checkIn = LocalDate.parse(r[3]);
                LocalDate checkOut = LocalDate.parse(r[4]);
                int guestCount = Integer.parseInt(r[5]);
                BigDecimal totalPrice = new BigDecimal(r[6]);
                ReservationStatus status = ReservationStatus.valueOf(r[7].toUpperCase());

                Optional<Guest> guestOpt = guestRepository.findById(guestId);
                Optional<Room> roomOpt = roomRepository.findByNumber(roomNumber);

                if (guestOpt.isEmpty()) {
                    System.err.println("Client introuvable (" + guestId + ") pour la réservation " + id);
                    continue;
                }
                if (roomOpt.isEmpty()) {
                    System.err.println("Chambre introuvable (" + roomNumber + ") pour la réservation " + id);
                    continue;
                }

                DateRange stay = new DateRange(checkIn, checkOut);
                Reservation reservation = new Reservation(id, guestOpt.get(), roomOpt.get(), stay, guestCount, totalPrice, status);
                reservationRepository.save(reservation);
                count++;
            } catch (Exception e) {
                System.err.println("Erreur à la lecture de la réservation ligne " + (i + 1) + " : " + e.getMessage());
            }
        }
        return count;
    }
}
