package com.hotel.loader;

import com.hotel.exception.DataLoadingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lit un fichier CSV ligne par ligne, gère les lignes vides ou les commentaires (#).
 */
public final class CsvReader {

    private CsvReader() {
    }

    public static List<String[]> read(Path path) throws DataLoadingException {
        if (!Files.exists(path)) {
            throw new DataLoadingException("Fichier CSV introuvable sur le disque : " + path);
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return readLines(reader, path.toString());
        } catch (IOException e) {
            throw new DataLoadingException("Erreur de lecture du fichier : " + path, e);
        }
    }

    public static List<String[]> readFromClasspath(String resourcePath) throws DataLoadingException {
        InputStream stream = CsvReader.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new DataLoadingException("Ressource CSV introuvable dans le classpath : " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return readLines(reader, resourcePath);
        } catch (IOException e) {
            throw new DataLoadingException("Erreur de lecture de la ressource : " + resourcePath, e);
        }
    }

    private static List<String[]> readLines(BufferedReader reader, String sourceName) throws IOException {
        List<String[]> rows = new ArrayList<>();
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            // Découpage simple sur virgule (les valeurs peuvent être entourées d'espaces)
            String[] tokens = Arrays.stream(line.split(",", -1))
                    .map(String::trim)
                    .toArray(String[]::new);
            rows.add(tokens);
        }
        return rows;
    }
}
