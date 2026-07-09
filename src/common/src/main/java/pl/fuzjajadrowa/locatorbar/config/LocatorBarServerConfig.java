package pl.fuzjajadrowa.locatorbar.config;

import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class LocatorBarServerConfig {
    private static final Path CONFIG_PATH = Path.of("config", "locatorbar-server.toml");
    public static final float INFINITE_PLAYER_HEAD_DISTANCE = 60_000_000.0F;
    private static ServerSettings data = null;

    private LocatorBarServerConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            data = ServerSettings.defaults();
            save();
            return;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            properties.load(new TomlPropertiesReader(reader));

            // Read PlayerMarkerType
            PlayerMarkerType playerMarkerType = PlayerMarkerType.HEADS;
            String playerMarkerTypeStr = properties.getProperty("playerMarkerType");
            if (playerMarkerTypeStr != null) {
                try {
                    playerMarkerType = PlayerMarkerType.valueOf(playerMarkerTypeStr.trim().replace("\"", "").toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException exception) {
                    playerMarkerType = PlayerMarkerType.HEADS;
                }
            } else {
                // Fallback to old boolean key
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

            data = new ServerSettings(
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
            save();
        } catch (IOException | IllegalArgumentException exception) {
            data = ServerSettings.defaults();
            save();
        }
    }

    public static void save() {
        if (data == null) {
            return;
        }

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                writer.write("# Locator Bar server-enforced settings\n");
                writer.write("version = 2\n");
                writer.write("# You can choose between \"reworked\" and \"classic\" style or just disable it with \"off\".\n");
                writer.write("style = \"" + data.style().name().toLowerCase(Locale.ROOT) + "\"\n");
                writer.write("# Show coordinates/days under locator bar. Works only on Reworked style.\n");
                writer.write("showCoordinates = " + data.showCoordinates() + "\n");
                writer.write("showDays = " + data.showDays() + "\n");
                writer.write("# Show world directions on locator bar.\n");
                writer.write("showWorldDirections = " + data.showWorldDirections() + "\n");
                writer.write("# Player marker style (\"heads\", \"dots\", or \"off\") and choose max visible players on it.\n");
                writer.write("playerMarkerType = \"" + data.playerMarkerType().name().toLowerCase(Locale.ROOT) + "\"\n");
                writer.write("maxVisiblePlayers = " + data.maxVisiblePlayers() + "\n");
                writer.write("# Player markers distance behaviour configuration. Fade start and fade to min is close range\n");
                writer.write("# when player marker opacity / size decreases down to min alpha value in percent / small dot. To disable this behaviour\n");
                writer.write("# set min alpha value to 100.0.\n");
                writer.write("# Marker hide distance is the distance when player marker completely disappears from locator bar.\n");
                writer.write("# You can set it to your own value or set \"inf\" to disable this behaviour.\n");
                writer.write("playerMarkerFadeStartDistance = " + formatDistance(data.playerMarkerFadeStartDistance()) + "\n");
                writer.write("playerMarkerFadeToMinDistance = " + formatDistance(data.playerMarkerFadeToMinDistance()) + "\n");
                writer.write("playerMarkerHideDistance = " + formatDistance(data.playerMarkerHideDistance()) + "\n");
                writer.write("playerMarkerMinAlphaPercent = " + data.playerMarkerMinAlphaPercent() + "\n");
                writer.write("# Show waypoints on locator bar and choose max visible waypoints on it.\n");
                writer.write("showWaypoints = " + data.showWaypoints() + "\n");
                writer.write("maxVisibleWaypoints = " + data.maxVisibleWaypoints() + "\n");
                writer.write("# Show death waypoint on locator bar.\n");
                writer.write("showDeathWaypoint = " + data.showDeathWaypoint() + "\n");
            }
        } catch (IOException ignored) {
            // Keep startup stable even if saving fails.
        }
    }

    public static ServerSettings get() {
        return data;
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

    private static String formatDistance(float distance) {
        return distance >= INFINITE_PLAYER_HEAD_DISTANCE ? "inf" : Float.toString(distance);
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

    private static final class TomlPropertiesReader extends Reader {
        private final Reader delegate;
        private String content;
        private int index;

        private TomlPropertiesReader(Reader delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (content == null) {
                content = normalize(delegate);
            }
            if (index >= content.length()) {
                return -1;
            }

            int count = Math.min(len, content.length() - index);
            content.getChars(index, index + count, cbuf, off);
            index += count;
            return count;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        private static String normalize(Reader reader) throws IOException {
            StringBuilder output = new StringBuilder();
            StringBuilder line = new StringBuilder();
            int read;
            while ((read = reader.read()) >= 0) {
                char character = (char) read;
                if (character == '\n') {
                    appendLine(output, line.toString());
                    line.setLength(0);
                } else if (character != '\r') {
                    line.append(character);
                }
            }
            appendLine(output, line.toString());
            return output.toString();
        }

        private static void appendLine(StringBuilder output, String line) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                return;
            }
            output.append(trimmed.replaceFirst("\\s*=\\s*", "=")).append('\n');
        }
    }
}