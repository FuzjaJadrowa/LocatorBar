package pl.fuzjajadrowa.locatorbar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;

final class ConfigFile<T> {
    static final Logger LOGGER = LoggerFactory.getLogger(LocatorBar.MOD_ID);
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
            LOGGER.warn("Invalid configuration: {}", path, exception);
            try {
                Path backup = Files.createTempFile(path.toAbsolutePath().getParent(), path.getFileName() + ".", ".bak");
                Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.warn("Configuration backup: {}", backup);
            } catch (IOException backupFailure) {
                writable = false;
                LOGGER.warn("Cannot back up configuration; preserving original: {}", path, backupFailure);
            }
        } catch (IOException exception) {
            writable = false;
            LOGGER.warn("Cannot read configuration; preserving original: {}", path, exception);
        }
        return defaults.get();
    }

    boolean save(T data) {
        if (!writable) {
            LOGGER.warn("Skipped configuration write to preserve unreadable original: {}", path);
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
            LOGGER.warn("Cannot save configuration: {}", path, exception);
            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException exception) {
                    LOGGER.warn("Cannot remove temporary configuration: {}", temporary, exception);
                }
            }
        }
    }
}