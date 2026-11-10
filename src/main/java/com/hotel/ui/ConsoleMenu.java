package com.hotel.ui;

import com.hotel.exception.HotelException;
import com.hotel.model.DateRange;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.pricing.PriceQuote;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.AvailabilityService;
import com.hotel.service.ReportService;
import com.hotel.service.ReservationService;
import com.hotel.util.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Interface console interactive (Menu).
 * Gère uniquement la saisie utilisateur et l'affichage (Single Responsibility).
 */
public class ConsoleMenu {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final AvailabilityService availabilityService;
    private final ReservationService reservationService;
    private final ReportService reportService;
    private final Scanner scanner;

    public ConsoleMenu(RoomRepository roomRepository,
                       GuestRepository guestRepository,
                       AvailabilityService availabilityService,
                       ReservationService reservationService,
                       ReportService reportService) {
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.availabilityService = availabilityService;
        this.reservationService = reservationService;
        this.reportService = reportService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("Votre choix : ").trim();
            System.out.println();
            switch (choice) {
                case "1" -> listRooms();
                case "2" -> listGuests();
                case "3" -> searchAvailableRooms();
                case "4" -> createReservation();
                case "5" -> listReservations();
                case "6" -> cancelReservation();
                case "7" -> checkIn();
                case "8" -> checkOut();
                case "9" -> showReports();
                case "0" -> {
                    System.out.println("Au revoir !");
                    running = false;
                }
                default -> System.out.println("Choix invalide. Veuillez réessayer.");
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("==================================================");
        System.out.println("         SYSTÈME DE GESTION HÔTELIÈRE             ");
        System.out.println("==================================================");
        System.out.println("1. Consulter la liste des chambres");
        System.out.println("2. Consulter la liste des clients");
        System.out.println("3. Rechercher des chambres disponibles");
        System.out.println("4. Créer une réservation");
        System.out.println("5. Afficher toutes les réservations");
        System.out.println("6. Annuler une réservation");
        System.out.println("7. Effectuer un Check-in");
        System.out.println("8. Effectuer un Check-out");
        System.out.println("9. Rapports de gestion (Chiffre d'affaires & Taux)");
        System.out.println("0. Quitter");
        System.out.println("--------------------------------------------------");
    }

    private void listRooms() {
        System.out.println("--- LISTE DES CHAMBRES ---");
        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            System.out.println("Aucune chambre enregistrée.");
            return;
        }
        for (Room r : rooms) {
            System.out.printf("Chambre %s | Étage %d | Type : %-8s | Capacité : %d pers | Base : %s | Équipements : %s%n",
                    r.getNumber(), r.getFloor(), r.getType().getLabel(), r.getCapacity(),
                    Money.format(r.getBasePrice()), r.getAmenities());
        }
    }

    private void listGuests() {
        System.out.println("--- LISTE DES CLIENTS ---");
        List<Guest> guests = guestRepository.findAll();
        if (guests.isEmpty()) {
            System.out.println("Aucun client enregistré.");
            return;
        }
        for (Guest g : guests) {
            System.out.printf("Client [%s] %-18s | Email : %-25s | Fidélité : %s (-%d%%)%n",
                    g.getId(), g.getFullName(), g.getEmail(),
                    g.getLoyaltyLevel().name(), g.getLoyaltyLevel().getDiscountPercent());
        }
    }

    private void searchAvailableRooms() {
        System.out.println("--- RECHERCHE DE DISPONIBILITÉS ---");
        try {
            DateRange stay = readDateRange();
            int guestCount = readInt("Nombre de personnes : ");

            List<Room> available = availabilityService.findAvailableRooms(stay, guestCount);
            if (available.isEmpty()) {
                System.out.println("Aucune chambre disponible pour ces critères.");
            } else {
                System.out.println(available.size() + " chambre(s) disponible(s) :");
                for (Room r : available) {
                    BigDecimal nightly = r.calculateNightlyPrice(guestCount);
                    System.out.printf("- Chambre %s (%s) : %s/nuit | Total estimé base : %s%n",
                            r.getNumber(), r.getType().getLabel(), Money.format(nightly),
                            Money.format(nightly.multiply(BigDecimal.valueOf(stay.nights()))));
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private void createReservation() {
        System.out.println("--- CRÉATION D'UNE RÉSERVATION ---");
        try {
            String guestId = prompt("ID du client (ex: G001) : ");
            String roomNumber = prompt("Numéro de la chambre (ex: 101) : ");
            DateRange stay = readDateRange();
            int guestCount = readInt("Nombre de personnes : ");

            PriceQuote quote = reservationService.calculateQuote(guestId, roomNumber, stay, guestCount);
            System.out.println("\nDevis calculé :");
            System.out.println("  Sous-total (" + stay.nights() + " nuits) : " + Money.format(quote.subtotal()));
            quote.adjustments().forEach((desc, amount) ->
                    System.out.println("  " + desc + " : " + (amount.signum() >= 0 ? "+" : "") + Money.format(amount)));
            System.out.println("  TOTAL FINAL : " + Money.format(quote.total()));

            String confirm = prompt("\nConfirmer la réservation ? (O/N) : ");
            if (confirm.equalsIgnoreCase("O") || confirm.equalsIgnoreCase("OUI")) {
                Reservation res = reservationService.createReservation(guestId, roomNumber, stay, guestCount);
                System.out.println("Réservation confirmée avec succès ! Numéro : " + res.getId());
            } else {
                System.out.println("Réservation annulée.");
            }
        } catch (HotelException e) {
            System.out.println("Erreur métier : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur de saisie : " + e.getMessage());
        }
    }

    private void listReservations() {
        System.out.println("--- LISTE DES RÉSERVATIONS ---");
        List<Reservation> list = reservationService.getAllReservations();
        if (list.isEmpty()) {
            System.out.println("Aucune réservation.");
            return;
        }
        for (Reservation r : list) {
            System.out.printf("[%s] %s pour %s | Chambre %s (%s) | Du %s (%d nuit(s)) | Total : %s | Statut : %s%n",
                    r.getId(), r.getGuest().getFullName(), r.getGuestCount() + " pers.",
                    r.getRoom().getNumber(), r.getRoom().getType().getLabel(),
                    r.getStay(), r.getStay().nights(),
                    Money.format(r.getTotalPrice()), r.getStatus().getLabel());
        }
    }

    private void cancelReservation() {
        System.out.println("--- ANNULATION D'UNE RÉSERVATION ---");
        String id = prompt("Identifiant de la réservation à annuler (ex: R001) : ");
        try {
            BigDecimal fee = reservationService.cancelReservation(id);
            System.out.println("Réservation " + id + " annulée.");
            if (fee.signum() > 0) {
                System.out.println("Frais d'annulation appliqués : " + Money.format(fee));
            } else {
                System.out.println("Annulation gratuite (aucun frais).");
            }
        } catch (HotelException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private void checkIn() {
        System.out.println("--- CHECK-IN ---");
        String id = prompt("Identifiant de la réservation : ");
        try {
            reservationService.checkIn(id);
            System.out.println("Check-in validé pour la réservation " + id + ". Bienvenue au client !");
        } catch (HotelException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private void checkOut() {
        System.out.println("--- CHECK-OUT ---");
        String id = prompt("Identifiant de la réservation : ");
        try {
            reservationService.checkOut(id);
            System.out.println("Check-out effectué avec succès pour la réservation " + id + ".");
        } catch (HotelException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private void showReports() {
        System.out.println("--- RAPPORTS DE GESTION ---");
        BigDecimal totalRevenue = reportService.calculateTotalRevenue();
        System.out.println("Chiffre d'affaires total encaissé : " + Money.format(totalRevenue));

        LocalDate today = LocalDate.now();
        double occupancyToday = reportService.calculateOccupancyRateOn(today);
        System.out.printf("Taux d'occupation aujourd'hui (%s) : %.1f%%%n", today, occupancyToday);

        LocalDate in3Days = today.plusDays(3);
        double occupancyFuture = reportService.calculateOccupancyRateOn(in3Days);
        System.out.printf("Taux d'occupation prévu dans 3 jours (%s) : %.1f%%%n", in3Days, occupancyFuture);
    }

    private DateRange readDateRange() {
        while (true) {
            try {
                String inStr = prompt("Date d'arrivée (AAAA-MM-JJ, ex: 2026-11-20) : ");
                LocalDate checkIn = LocalDate.parse(inStr.trim(), DATE_FORMATTER);
                String outStr = prompt("Date de départ (AAAA-MM-JJ, ex: 2026-11-23) : ");
                LocalDate checkOut = LocalDate.parse(outStr.trim(), DATE_FORMATTER);
                return new DateRange(checkIn, checkOut);
            } catch (DateTimeParseException e) {
                System.out.println("Format de date incorrect. Utilisez le format AAAA-MM-JJ (ex: 2026-11-20).");
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private int readInt(String promptMessage) {
        while (true) {
            String val = prompt(promptMessage).trim();
            try {
                int n = Integer.parseInt(val);
                if (n > 0) return n;
                System.out.println("Veuillez saisir un nombre strictement positif.");
            } catch (NumberFormatException e) {
                System.out.println("Nombre invalide.");
            }
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }
}
