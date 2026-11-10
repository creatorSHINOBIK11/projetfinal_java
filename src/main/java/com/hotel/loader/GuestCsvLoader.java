package com.hotel.loader;

import com.hotel.exception.DataLoadingException;
import com.hotel.model.Guest;
import com.hotel.model.LoyaltyLevel;
import com.hotel.repository.GuestRepository;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Charge les clients depuis un CSV :
 * format attendu : id,fullName,email,loyaltyLevel
 */
public class GuestCsvLoader {

    private final GuestRepository guestRepository;

    public GuestCsvLoader(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
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
            if (r.length < 4) {
                System.err.println("Ligne client invalide : " + Arrays.toString(r));
                continue;
            }
            try {
                String id = r[0];
                String name = r[1];
                String email = r[2];
                LoyaltyLevel level = LoyaltyLevel.valueOf(r[3].toUpperCase());

                Guest guest = new Guest(id, name, email, level);
                guestRepository.save(guest);
                count++;
            } catch (Exception e) {
                System.err.println("Erreur à la lecture du client ligne " + (i + 1) + " : " + e.getMessage());
            }
        }
        return count;
    }
}
