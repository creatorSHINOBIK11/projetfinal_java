package com.hotel;

import com.hotel.loader.GuestCsvLoader;
import com.hotel.loader.ReservationCsvLoader;
import com.hotel.loader.RoomCsvLoader;
import com.hotel.pricing.HighSeasonSurcharge;
import com.hotel.pricing.LongStayDiscount;
import com.hotel.pricing.LoyaltyDiscount;
import com.hotel.pricing.PriceCalculator;
import com.hotel.pricing.PricingRule;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.InMemoryGuestRepository;
import com.hotel.repository.InMemoryReservationRepository;
import com.hotel.repository.InMemoryRoomRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.AvailabilityService;
import com.hotel.service.CancellationPolicy;
import com.hotel.service.ReportService;
import com.hotel.service.ReservationService;
import com.hotel.ui.ConsoleMenu;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Month;
import java.util.List;
import java.util.Set;

/**
 * Point d'entrée de l'application.
 * Son unique rôle est d'assembler (injecter) les dépendances et de lancer l'application.
 * Elle ne contient aucune logique métier ni affichage de menu (respect du principe SRP).
 */
public class Application {

    public static void main(String[] args) {
        System.out.println("Initialisation du système de gestion hôtelière...");

        // 1. Repositories (Stockage en mémoire)
        RoomRepository roomRepository = new InMemoryRoomRepository();
        GuestRepository guestRepository = new InMemoryGuestRepository();
        ReservationRepository reservationRepository = new InMemoryReservationRepository();

        // 2. Chargement des données initiales depuis les fichiers CSV (exigence du projet)
        loadInitialData(roomRepository, guestRepository, reservationRepository, args);

        // 3. Règles de tarification (Polymorphisme & Open/Closed Principle)
        List<PricingRule> pricingRules = List.of(
                new LoyaltyDiscount(),
                new LongStayDiscount(7, 10), // -10% dès 7 nuits
                new HighSeasonSurcharge(Set.of(Month.JULY, Month.AUGUST), 20) // +20% en été
        );
        PriceCalculator priceCalculator = new PriceCalculator(pricingRules);

        // 4. Services Métier
        Clock clock = Clock.systemDefaultZone();
        AvailabilityService availabilityService = new AvailabilityService(roomRepository, reservationRepository);
        CancellationPolicy cancellationPolicy = new CancellationPolicy(2, 50); // Gratuit si > 48h, sinon 50%
        ReservationService reservationService = new ReservationService(
                reservationRepository,
                roomRepository,
                guestRepository,
                availabilityService,
                priceCalculator,
                cancellationPolicy,
                clock
        );
        ReportService reportService = new ReportService(roomRepository, reservationRepository);

        // 5. Interface Console
        ConsoleMenu menu = new ConsoleMenu(
                roomRepository,
                guestRepository,
                availabilityService,
                reservationService,
                reportService
        );

        menu.start();
    }

    private static void loadInitialData(RoomRepository roomRepository,
                                       GuestRepository guestRepository,
                                       ReservationRepository reservationRepository,
                                       String[] args) {
        RoomCsvLoader roomLoader = new RoomCsvLoader(roomRepository);
        GuestCsvLoader guestLoader = new GuestCsvLoader(guestRepository);
        ReservationCsvLoader reservationLoader = new ReservationCsvLoader(reservationRepository, guestRepository, roomRepository);

        try {
            // Si un répertoire de données est passé en argument CLI, on l'utilise
            if (args.length > 0) {
                Path baseDir = Paths.get(args[0]);
                int rooms = roomLoader.load(baseDir.resolve("rooms.csv"));
                int guests = guestLoader.load(baseDir.resolve("guests.csv"));
                int res = reservationLoader.load(baseDir.resolve("reservations.csv"));
                System.out.printf("Données chargées depuis %s : %d chambres, %d clients, %d réservations.%n",
                        baseDir, rooms, guests, res);
            } else {
                // Sinon, chargement automatique depuis les ressources incluses dans le JAR
                int rooms = roomLoader.loadFromClasspath("/data/rooms.csv");
                int guests = guestLoader.loadFromClasspath("/data/guests.csv");
                int res = reservationLoader.loadFromClasspath("/data/reservations.csv");
                System.out.printf("Données initiales chargées par défaut : %d chambres, %d clients, %d réservations.%n",
                        rooms, guests, res);
            }
        } catch (Exception e) {
            System.err.println("Avertissement lors du chargement des données : " + e.getMessage());
        }
    }
}
