package com.hotel.loader;

import com.hotel.exception.DataLoadingException;
import com.hotel.model.Amenity;
import com.hotel.model.DeluxeRoom;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.model.StandardRoom;
import com.hotel.model.Suite;
import com.hotel.repository.RoomRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Charge les chambres depuis un CSV :
 * format attendu : number,floor,type,basePrice,capacity,amenities(séparés par ;)
 */
public class RoomCsvLoader {

    private final RoomRepository roomRepository;

    public RoomCsvLoader(RoomRepository roomRepository) {
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

    private int parseRows(List<String[]> rows) throws DataLoadingException {
        if (rows.isEmpty()) {
            return 0;
        }
        // Première ligne = en-tête (on l'ignore si elle commence par "number")
        int startIndex = 0;
        if (rows.get(0)[0].equalsIgnoreCase("number")) {
            startIndex = 1;
        }

        int count = 0;
        for (int i = startIndex; i < rows.size(); i++) {
            String[] r = rows.get(i);
            if (r.length < 5) {
                System.err.println("Ligne chambre invalide (colonnes insuffisantes) : " + Arrays.toString(r));
                continue;
            }
            try {
                String number = r[0];
                int floor = Integer.parseInt(r[1]);
                RoomType type = RoomType.valueOf(r[2].toUpperCase());
                BigDecimal basePrice = new BigDecimal(r[3]);
                int capacity = Integer.parseInt(r[4]);

                Set<Amenity> amenities = EnumSet.noneOf(Amenity.class);
                if (r.length >= 6 && !r[5].isBlank()) {
                    amenities = Arrays.stream(r[5].split(";"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> Amenity.valueOf(s.toUpperCase()))
                            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Amenity.class)));
                }

                Room room = switch (type) {
                    case STANDARD -> new StandardRoom(number, floor, basePrice, capacity, amenities);
                    case DELUXE -> new DeluxeRoom(number, floor, basePrice, capacity, amenities);
                    case SUITE -> new Suite(number, floor, basePrice, capacity, amenities);
                };
                roomRepository.save(room);
                count++;
            } catch (Exception e) {
                System.err.println("Erreur à la lecture de la chambre ligne " + (i + 1) + " : " + e.getMessage());
            }
        }
        return count;
    }
}
