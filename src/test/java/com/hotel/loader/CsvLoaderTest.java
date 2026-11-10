package com.hotel.loader;

import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.repository.InMemoryRoomRepository;
import com.hotel.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour le chargement CSV")
class CsvLoaderTest {

    @Test
    @DisplayName("Devrait charger correctement les chambres depuis le fichier CSV de test")
    void shouldLoadRoomsFromCsvClasspath() throws Exception {
        RoomRepository repository = new InMemoryRoomRepository();
        RoomCsvLoader loader = new RoomCsvLoader(repository);

        int loadedCount = loader.loadFromClasspath("/data/rooms.csv");
        assertTrue(loadedCount >= 7, "Au moins 7 chambres doivent être chargées");

        Optional<Room> suite301 = repository.findByNumber("301");
        assertTrue(suite301.isPresent());
        assertEquals(RoomType.SUITE, suite301.get().getType());
        assertEquals(new BigDecimal("250.00"), suite301.get().getBasePrice());
    }
}
