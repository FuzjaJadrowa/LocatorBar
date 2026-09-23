package pl.fuzjajadrowa.locatorbar.config;

import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class LocatorBarServerConfig {
    private static final Path TOML_CONFIG_PATH = Path.of("config", "locatorbar-server.toml");
    private static final Path JSON_CONFIG_PATH = Path.of("config", "locatorbar-server.json");
    public static final float INFINITE_PLAYER_HEAD_DISTANCE = 60_000_000.0F;
    private static ServerSettings data = null;

    private LocatorBarServerConfig() {
    }

    private static final ConfigFile<ServerSettings> FILE = new ConfigFile<>(JSON_CONFIG_PATH, ServerSettings.class);

    public static void load() {
        if (!Files.exists(JSON_CONFIG_PATH)) {
            ServerSettings migrated = loadFromLegacyToml();
            data = migrated == null ? ServerSettings.defaults() : migrated;
            if (FILE.save(data) && migrated != null) {
                try {
                    Files.deleteIfExists(TOML_CONFIG_PATH);
                } catch (IOException exception) {
                    ConfigFile.LOGGER.log(java.util.logging.Level.WARNING, "Cannot remove legacy configuration: " + TOML_CONFIG_PATH, exception);
                }
            }
            return;
        }
        data = FILE.read(ServerSettings::defaults);
        save();
    }

    public static void save() {
        if (data != null) FILE.save(data);
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
        } catch (IOException | IllegalArgumentException e) {
            ConfigFile.LOGGER.log(java.util.logging.Level.WARNING, "Cannot migrate configuration: " + TOML_CONFIG_PATH, e);
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
            return clamp(parsed, min, max);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Float.isNaN(value) ? min : Math.max(min, Math.min(max, value));
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

        public ServerSettings {
            style = style == null ? LocatorBarStyle.REWORKED : style;
            playerMarkerType = playerMarkerType == null ? PlayerMarkerType.HEADS : playerMarkerType;
            maxVisiblePlayers = clampInt(maxVisiblePlayers, 1, 64);
            maxVisibleWaypoints = clampInt(maxVisibleWaypoints, 1, 64);
            playerMarkerFadeStartDistance = clamp(playerMarkerFadeStartDistance, 0, INFINITE_PLAYER_HEAD_DISTANCE);
            playerMarkerFadeToMinDistance = clamp(playerMarkerFadeToMinDistance, playerMarkerFadeStartDistance, INFINITE_PLAYER_HEAD_DISTANCE);
            playerMarkerHideDistance = clamp(playerMarkerHideDistance, playerMarkerFadeToMinDistance, INFINITE_PLAYER_HEAD_DISTANCE);
            playerMarkerMinAlphaPercent = clamp(playerMarkerMinAlphaPercent, 0, 100);
        }

        public ServerSettings update(java.util.function.Consumer<Builder> change) {
            Builder builder = new Builder(this);
            change.accept(builder);
            return builder.build();
        }

        public static final class Builder {
            public LocatorBarStyle style;
            public boolean showCoordinates;
            public boolean showDays;
            public boolean showWorldDirections;
            public PlayerMarkerType playerMarkerType;
            public int maxVisiblePlayers;
            public float playerMarkerFadeStartDistance;
            public float playerMarkerFadeToMinDistance;
            public float playerMarkerHideDistance;
            public float playerMarkerMinAlphaPercent;
            public boolean showWaypoints;
            public int maxVisibleWaypoints;
            public boolean showDeathWaypoint;

            private Builder(ServerSettings settings) {
                style = settings.style();
                showCoordinates = settings.showCoordinates();
                showDays = settings.showDays();
                showWorldDirections = settings.showWorldDirections();
                playerMarkerType = settings.playerMarkerType();
                maxVisiblePlayers = settings.maxVisiblePlayers();
                playerMarkerFadeStartDistance = settings.playerMarkerFadeStartDistance();
                playerMarkerFadeToMinDistance = settings.playerMarkerFadeToMinDistance();
                playerMarkerHideDistance = settings.playerMarkerHideDistance();
                playerMarkerMinAlphaPercent = settings.playerMarkerMinAlphaPercent();
                showWaypoints = settings.showWaypoints();
                maxVisibleWaypoints = settings.maxVisibleWaypoints();
                showDeathWaypoint = settings.showDeathWaypoint();
            }

            public ServerSettings build() {
                return new ServerSettings(style, showCoordinates, showDays, showWorldDirections, playerMarkerType, maxVisiblePlayers, playerMarkerFadeStartDistance, playerMarkerFadeToMinDistance, playerMarkerHideDistance, playerMarkerMinAlphaPercent, showWaypoints, maxVisibleWaypoints, showDeathWaypoint);
            }
        }

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