package pl.fuzjajadrowa.locatorbar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LocatorBarConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "locatorbar.json");
    private static LocatorBarConfigData data = new LocatorBarConfigData();

    private LocatorBarConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            LocatorBarConfigData loaded = GSON.fromJson(reader, LocatorBarConfigData.class);
            data = loaded == null ? new LocatorBarConfigData() : loaded;
            if (data.style == null) {
                data.style = LocatorBarStyle.REWORKED;
            }
        } catch (IOException | JsonParseException exception) {
            data = new LocatorBarConfigData();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException ignored) {
            // Keep runtime behavior stable even if saving fails.
        }
    }

    public static LocatorBarStyle getStyle() {
        return data.style;
    }

    public static void setStyle(LocatorBarStyle style) {
        data.style = style;
    }

    public static boolean isEnabled() {
        return getStyle() != LocatorBarStyle.OFF;
    }

    private static final class LocatorBarConfigData {
        @SerializedName("style")
        private LocatorBarStyle style = LocatorBarStyle.REWORKED;
    }
}