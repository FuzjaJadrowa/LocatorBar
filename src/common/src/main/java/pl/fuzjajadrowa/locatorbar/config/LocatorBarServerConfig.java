package pl.fuzjajadrowa.locatorbar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class LocatorBarServerConfig {
    private static final Path TOML_CONFIG_PATH = Path.of("config", "locatorbar-server.toml");
    private static final Path JSON_CONFIG_PATH = Path.of("config", "locatorbar-server.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final float INFINITE_PLAYER_HEAD_DISTANCE = 60_000_000.0F;
    private static ServerSettings data = null;

    private LocatorBarServerConfig() {
    }

    public static void load() {
        if (!Files.exists(JSON_CONFIG_PATH)) {
            ServerSettings migrated = loadFromLegacyToml();
            if (migrated != null) {
                data = migrated;
                try {
                    Files.deleteIfExists(TOML_CONFIG_PATH);
                } catch (IOException ignored) {
                }
            } else {
                data = ServerSettings.defaults();
            }
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(JSON_CONFIG_PATH)) {
            ServerSettings loaded = GSON.fromJson(reader, ServerSettings.class);
            data = loaded == null ? ServerSettings.defaults() : loaded;

            data = new ServerSettings(
                    data.style() == null ? LocatorBarStyle.REWORKED : data.style(),
                    data.showCoordinates(),
                    data.showDays(),
                    data.showWorldDirections(),
                    data.playerMarkerType() == null ? PlayerMarkerType.HEADS : data.playerMarkerType(),
                    clampInt(data.maxVisiblePlayers(), 1, 64),
                    clamp(data.playerMarkerFadeStartDistance(), 0.0F, INFINITE_PLAYER_HEAD_DISTANCE),
                    clamp(data.playerMarkerFadeToMinDistance(), data.playerMarkerFadeStartDistance(), INFINITE_PLAYER_HEAD_DISTANCE),
                    clamp(data.playerMarkerHideDistance(), data.playerMarkerFadeToMinDistance(), INFINITE_PLAYER_HEAD_DISTANCE),
                    clamp(data.playerMarkerMinAlphaPercent(), 0.0F, 100.0F),
                    data.showWaypoints(),
                    clampInt(data.maxVisibleWaypoints(), 1, 64),
                    data.showDeathWaypoint()
            );
            save();
        } catch (IOException | com.google.gson.JsonParseException exception) {
            data = ServerSettings.defaults();
            save();
        }
    }

    public static void save() {
        if (data == null) {
            return;
        }

        try {
            Files.createDirectories(JSON_CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(JSON_CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static ServerSettings get() {
        return data;
    }

    public static void set(ServerSettings settings) {
        data = settings;
    }

    private static ServerSettings loadFromLegacyToml() {
        if (!Files.exists(TOML_CONFIG_PATH)) {
            return null;
        }
        try {
            Properties properties = new Properties();
            List<String> lines = Files.readAllLines(TOML_CONFIG_PATH);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] parts = trimmed.split("=", 2);
                if (parts.length == 2) {
                    properties.setProperty(parts[0].trim(), parts[1].trim());
                }
            }

            PlayerMarkerType playerMarkerType = PlayerMarkerType.HEADS;
            String playerMarkerTypeStr = properties.getProperty("playerMarkerType");
            if (playerMarkerTypeStr != null) {
                try {
                    playerMarkerType = PlayerMarkerType.valueOf(playerMarkerTypeStr.trim().replace("\"", "").toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                    playerMarkerType = PlayerMarkerType.HEADS;
                }
            } else {
                String showPlayerHeadsStr = properties.getProperty("showPlayerHeads");
                if (showPlayerHeadsStr != null) {
                    boolean showPlayerHeads = Boolean.parseBoolean(showPlayerHeadsStr.trim());
                    playerMarkerType = showPlayerHeads ? PlayerMarkerType.HEADS : PlayerMarkerType.OFF;
                }
            }

            float playerMarkerFadeStartDistance = readDistance(properties, "playerMarkerFadeStartDistance",
                    readDistance(properties, "playerHeadFadeStartDistance", ServerSettings.DEFAULT_PLAYER_MARKER_FADE_START_DISTANCE, 0.0F), 0.0F);
            float playerMarkerFadeToMinDistance = readDistance(properties, "playerMarkerFadeToMinDistance",
                    readDistance(properties, "playerHeadFadeToMinDistance", ServerSettings.DEFAULT_PLAYER_MARKER_FADE_TO_MIN_DISTANCE, playerMarkerFadeStartDistance), playerMarkerFadeStartDistance);
            float playerMarkerHideDistance = readDistance(properties, "playerMarkerHideDistance",
                    readDistance(properties, "playerHeadHideDistance", ServerSettings.DEFAULT_PLAYER_MARKER_HIDE_DISTANCE, playerMarkerFadeToMinDistance), playerMarkerFadeToMinDistance);

            return new ServerSettings(
                    readStyle(properties, "style", LocatorBarStyle.REWORKED),
                    readBoolean(properties, "showCoordinates", true),
                    readBoolean(properties, "showDays", false),
                    readBoolean(properties, "showWorldDirections", true),
                    playerMarkerType,
                    readInt(properties, "maxVisiblePlayers", 16, 1, 64),
                    playerMarkerFadeStartDistance,
                    playerMarkerFadeToMinDistance,
                    playerMarkerHideDistance,
                    readFloat(properties, "playerMarkerMinAlphaPercent",
                            readFloat(properties, "playerHeadMinAlphaPercent", ServerSettings.DEFAULT_PLAYER_MARKER_MIN_ALPHA_PERCENT, 0.0F, 100.0F), 0.0F, 100.0F),
                    readBoolean(properties, "showWaypoints", true),
                    readInt(properties, "maxVisibleWaypoints", 16, 1, 64),
                    readBoolean(properties, "showDeathWaypoint", true)
            );
        } catch (Exception e) {
            return null;
        }
    }

    private static LocatorBarStyle readStyle(Properties properties, String key, LocatorBarStyle fallback) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        try {
            return LocatorBarStyle.valueOf(value.trim().replace("\"", "").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value.trim());
    }

    private static int readInt(Properties properties, String key, int fallback, int min, int max) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return Math.max(min, Math.min(max, parsed));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static float readDistance(Properties properties, String key, float fallback, float min) {
        String value = properties.getProperty(key);
        if (value != null && value.trim().replace("\"", "").equalsIgnoreCase("inf")) {
            return INFINITE_PLAYER_HEAD_DISTANCE;
        }
        return readFloat(properties, key, fallback, min, INFINITE_PLAYER_HEAD_DISTANCE);
    }

    private static float readFloat(Properties properties, String key, float fallback, float min, float max) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallback;
        }
        try {
            float parsed = Float.parseFloat(value.trim().replace("\"", ""));
            return Math.max(min, Math.min(max, parsed));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public record ServerSettings(
            LocatorBarStyle style,
            boolean showCoordinates,
            boolean showDays,
            boolean showWorldDirections,
            PlayerMarkerType playerMarkerType,
            int maxVisiblePlayers,
            float playerMarkerFadeStartDistance,
            float playerMarkerFadeToMinDistance,
            float playerMarkerHideDistance,
            float playerMarkerMinAlphaPercent,
            boolean showWaypoints,
            int maxVisibleWaypoints,
            boolean showDeathWaypoint
    ) {
        public static final float DEFAULT_PLAYER_MARKER_FADE_START_DISTANCE = 150.0F;
        public static final float DEFAULT_PLAYER_MARKER_FADE_TO_MIN_DISTANCE = 350.0F;
        public static final float DEFAULT_PLAYER_MARKER_HIDE_DISTANCE = INFINITE_PLAYER_HEAD_DISTANCE;
        public static final float DEFAULT_PLAYER_MARKER_MIN_ALPHA_PERCENT = 40.0F;

        public static ServerSettings defaults() {
            return new ServerSettings(
                    LocatorBarStyle.REWORKED,
                    true,
                    false,
                    true,
                    PlayerMarkerType.HEADS,
                    16,
                    DEFAULT_PLAYER_MARKER_FADE_START_DISTANCE,
                    DEFAULT_PLAYER_MARKER_FADE_TO_MIN_DISTANCE,
                    DEFAULT_PLAYER_MARKER_HIDE_DISTANCE,
                    DEFAULT_PLAYER_MARKER_MIN_ALPHA_PERCENT,
                    true,
                    16,
                    true
            );
        }
    }
}