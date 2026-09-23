package pl.fuzjajadrowa.locatorbar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ConfigFile<T> {
    static final Logger LOGGER = Logger.getLogger("locatorbar.config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;
    private final Class<T> type;
    private boolean writable = true;

    ConfigFile(Path path, Class<T> type) {
        this.path = path;
        this.type = type;
    }

    T read(Supplier<T> defaults) {
        writable = true;
        if (!Files.exists(path)) return defaults.get();
        try (Reader reader = Files.newBufferedReader(path)) {
            T result = GSON.fromJson(reader, type);
            if (result == null) throw new JsonParseException("Empty configuration");
            return result;
        } catch (JsonParseException exception) {
            LOGGER.log(Level.WARNING, "Invalid configuration: " + path, exception);
            try {
                Path backup = Files.createTempFile(path.toAbsolutePath().getParent(), path.getFileName() + ".", ".bak");
                Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.warning("Configuration backup: " + backup);
            } catch (IOException backupFailure) {
                writable = false;
                LOGGER.log(Level.WARNING, "Cannot back up configuration; preserving original: " + path, backupFailure);
            }
        } catch (IOException exception) {
            writable = false;
            LOGGER.log(Level.WARNING, "Cannot read configuration; preserving original: " + path, exception);
        }
        return defaults.get();
    }

    boolean save(T data) {
        if (!writable) {
            LOGGER.warning("Skipped configuration write to preserve unreadable original: " + path);
            return false;
        }
        Path temporary = null;
        try {
            Path destination = path.toAbsolutePath();
            Files.createDirectories(destination.getParent());
            temporary = Files.createTempFile(destination.getParent(), path.getFileName() + ".", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(data, writer);
            }
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Cannot save configuration: " + path, exception);
            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException exception) {
                    LOGGER.log(Level.WARNING, "Cannot remove temporary configuration: " + temporary, exception);
                }
            }
        }
    }
}