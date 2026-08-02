package pl.fuzjajadrowa.locatorbar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.CoordinatesFormat;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.DaysDisplayOrder;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LocatorBarConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "locatorbar.json");
    public static final float INFINITE_PLAYER_HEAD_DISTANCE = 60_000_000.0F;
    private static LocatorBarConfigData data = new LocatorBarConfigData();
    private static ServerSettings serverSettings;

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
            if (data.coordinatesFormat == null) {
                data.coordinatesFormat = CoordinatesFormat.XYZ;
            }
            if (data.daysDisplayOrder == null) {
                data.daysDisplayOrder = DaysDisplayOrder.DAYS_UNDER_COORDS;
            }
            if (data.waypoints == null) {
                data.waypoints = new HashMap<>();
            }

            if (data.version < 2) {
                if (!data.showPlayerHeads) {
                    data.playerMarkerType = PlayerMarkerType.OFF;
                } else {
                    data.playerMarkerType = PlayerMarkerType.HEADS;
                }
                data.playerMarkersScale = data.playerHeadsScale;
                data.playerMarkerOutline = data.playerHeadOutline;
                data.version = 2;
            }

            data.scale = clamp(data.scale, 0.5F, 2.0F);
            data.customOffsetX = clampInt(data.customOffsetX, -500, 500);
            data.customOffsetY = clampInt(data.customOffsetY, -500, 500);
            data.viewAngle = clamp(data.viewAngle, 30.0F, 180.0F);
            data.worldDirectionsScale = clamp(data.worldDirectionsScale, 0.5F, 2.0F);
            data.playerMarkersScale = clamp(data.playerMarkersScale, 0.5F, 2.0F);
            data.playerMarkerFadeStartDistance = clamp(data.playerMarkerFadeStartDistance, 0.0F, INFINITE_PLAYER_HEAD_DISTANCE);
            data.playerMarkerFadeToMinDistance = clamp(data.playerMarkerFadeToMinDistance, data.playerMarkerFadeStartDistance, INFINITE_PLAYER_HEAD_DISTANCE);
            data.playerMarkerHideDistance = clamp(data.playerMarkerHideDistance, data.playerMarkerFadeToMinDistance, INFINITE_PLAYER_HEAD_DISTANCE);
            data.playerMarkerMinAlphaPercent = clamp(data.playerMarkerMinAlphaPercent, 0.0F, 100.0F);
            data.maxVisiblePlayers = clampInt(data.maxVisiblePlayers, 1, 64);
            data.waypointsScale = clamp(data.waypointsScale, 0.5F, 2.0F);
            data.maxVisibleWaypoints = clampInt(data.maxVisibleWaypoints, 1, 64);
            
            save(); // save migrated fields
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
        return serverSettings == null ? data.style : serverSettings.style();
    }

    public static void setStyle(LocatorBarStyle style) {
        data.style = style;
    }

    public static boolean isEnabled() {
        return getStyle() != LocatorBarStyle.OFF;
    }

    public static float getScale() {
        return data.scale;
    }

    public static void setScale(float scale) {
        data.scale = clamp(scale, 0.5F, 2.0F);
    }

    public static int getCustomOffsetX() {
        return data.customOffsetX;
    }

    public static void setCustomOffsetX(int customOffsetX) {
        data.customOffsetX = clampInt(customOffsetX, -500, 500);
    }

    public static int getCustomOffsetY() {
        return data.customOffsetY;
    }

    public static void setCustomOffsetY(int customOffsetY) {
        data.customOffsetY = clampInt(customOffsetY, -500, 500);
    }

    public static float getViewAngle() {
        return data.viewAngle;
    }

    public static void setViewAngle(float viewAngle) {
        data.viewAngle = clamp(viewAngle, 30.0F, 180.0F);
    }

    public static boolean isShowCoordinates() {
        return serverSettings == null ? data.showCoordinates : (serverSettings.showCoordinates() && data.showCoordinates);
    }

    public static void setShowCoordinates(boolean showCoordinates) {
        data.showCoordinates = showCoordinates;
    }

    public static boolean isElementsOnXpBar() {
        return data.elementsOnXpBar;
    }

    public static void setElementsOnXpBar(boolean elementsOnXpBar) {
        data.elementsOnXpBar = elementsOnXpBar;
    }

    public static CoordinatesFormat getCoordinatesFormat() {
        return data.coordinatesFormat;
    }

    public static void setCoordinatesFormat(CoordinatesFormat coordinatesFormat) {
        data.coordinatesFormat = coordinatesFormat;
    }

    public static boolean isShowDays() {
        return serverSettings == null ? data.showDays : (serverSettings.showDays() && data.showDays);
    }

    public static void setShowDays(boolean showDays) {
        data.showDays = showDays;
    }

    public static DaysDisplayOrder getDaysDisplayOrder() {
        return data.daysDisplayOrder;
    }

    public static void setDaysDisplayOrder(DaysDisplayOrder daysDisplayOrder) {
        data.daysDisplayOrder = daysDisplayOrder;
    }

    public static boolean isShowWorldDirections() {
        return data.showWorldDirections;
    }

    public static void setShowWorldDirections(boolean showWorldDirections) {
        data.showWorldDirections = showWorldDirections;
    }

    public static float getWorldDirectionsScale() {
        return data.worldDirectionsScale;
    }

    public static void setWorldDirectionsScale(float worldDirectionsScale) {
        data.worldDirectionsScale = clamp(worldDirectionsScale, 0.5F, 2.0F);
    }

    public static PlayerMarkerType getPlayerMarkerType() {
        if (serverSettings == null) {
            return data.playerMarkerType;
        }
        PlayerMarkerType client = data.playerMarkerType;
        PlayerMarkerType server = serverSettings.playerMarkerType();
        if (server == PlayerMarkerType.OFF || client == PlayerMarkerType.OFF) {
            return PlayerMarkerType.OFF;
        }
        if (server == PlayerMarkerType.DOTS || client == PlayerMarkerType.DOTS) {
            return PlayerMarkerType.DOTS;
        }
        return PlayerMarkerType.HEADS;
    }

    public static void setPlayerMarkerType(PlayerMarkerType type) {
        data.playerMarkerType = type;
    }

    public static float getPlayerMarkersScale() {
        return data.playerMarkersScale;
    }

    public static void setPlayerMarkersScale(float scale) {
        data.playerMarkersScale = clamp(scale, 0.5F, 2.0F);
    }

    public static boolean isPlayerMarkerOutline() {
        return data.playerMarkerOutline;
    }

    public static void setPlayerMarkerOutline(boolean outline) {
        data.playerMarkerOutline = outline;
    }

    public static int getMaxVisiblePlayers() {
        return serverSettings == null ? data.maxVisiblePlayers : Math.min(data.maxVisiblePlayers, serverSettings.maxVisiblePlayers());
    }

    public static void setMaxVisiblePlayers(int maxVisiblePlayers) {
        data.maxVisiblePlayers = clampInt(maxVisiblePlayers, 1, 64);
    }

    public static float getPlayerMarkerFadeStartDistance() {
        return serverSettings == null ? data.playerMarkerFadeStartDistance : serverSettings.playerMarkerFadeStartDistance();
    }

    public static void setPlayerMarkerFadeStartDistance(float val) {
        data.playerMarkerFadeStartDistance = clamp(val, 0.0F, INFINITE_PLAYER_HEAD_DISTANCE);
    }

    public static float getPlayerMarkerFadeToMinDistance() {
        return serverSettings == null ? data.playerMarkerFadeToMinDistance : serverSettings.playerMarkerFadeToMinDistance();
    }

    public static float getPlayerMarkerHideDistance() {
        return serverSettings == null ? data.playerMarkerHideDistance : serverSettings.playerMarkerHideDistance();
    }

    public static float getPlayerMarkerMinAlpha() {
        float percent = serverSettings == null ? data.playerMarkerMinAlphaPercent : serverSettings.playerMarkerMinAlphaPercent();
        return clamp(percent, 0.0F, 100.0F) / 100.0F;
    }

    public static boolean isShowWaypoints() {
        return serverSettings == null ? data.showWaypoints : (serverSettings.showWaypoints() && data.showWaypoints);
    }

    public static void setShowWaypoints(boolean showWaypoints) {
        data.showWaypoints = showWaypoints;
    }

    public static boolean isShowDeathWaypoint() {
        return serverSettings == null ? data.showDeathWaypoint : (serverSettings.showDeathWaypoint() && data.showDeathWaypoint);
    }

    public static void setShowDeathWaypoint(boolean showDeathWaypoint) {
        data.showDeathWaypoint = showDeathWaypoint;
    }

    public static float getWaypointsScale() {
        return data.waypointsScale;
    }

    public static void setWaypointsScale(float waypointsScale) {
        data.waypointsScale = clamp(waypointsScale, 0.5F, 2.0F);
    }

    public static int getMaxVisibleWaypoints() {
        return serverSettings == null ? data.maxVisibleWaypoints : Math.min(data.maxVisibleWaypoints, serverSettings.maxVisibleWaypoints());
    }

    public static void setMaxVisibleWaypoints(int maxVisibleWaypoints) {
        data.maxVisibleWaypoints = clampInt(maxVisibleWaypoints, 1, 64);
    }

    public static Map<UUID, WaypointConfig> getWaypoints() {
        return data.waypoints;
    }

    public static WaypointConfig getWaypointConfig(UUID id) {
        return data.waypoints.get(id);
    }

    public static void setWaypointConfig(UUID id, WaypointConfig config) {
        data.waypoints.put(id, config);
    }

    public static void removeWaypointConfig(UUID id) {
        data.waypoints.remove(id);
    }

    public static boolean hasServerSettings() {
        return serverSettings != null;
    }

    public static void applyServerSettings(ServerSettings settings) {
        serverSettings = settings;
    }

    public static void clearServerSettings() {
        serverSettings = null;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class WaypointConfig {
        @SerializedName("world")
        public String world;
        @SerializedName("color")
        public int color;
        @SerializedName("character")
        public String character;
        @SerializedName("visible")
        public boolean visible = true;

        public WaypointConfig(String world, int color, String character, boolean visible) {
            this.world = world;
            this.color = color;
            this.character = character == null || character.isEmpty() ? null : character.substring(0, 1);
            this.visible = visible;
        }
    }

    private static final class LocatorBarConfigData {
        @SerializedName("version")
        private int version = 2;

        @SerializedName("style")
        private LocatorBarStyle style = LocatorBarStyle.REWORKED;

        @SerializedName("scale")
        private float scale = 1.0F;

        @SerializedName("customOffsetX")
        private int customOffsetX = 0;

        @SerializedName("customOffsetY")
        private int customOffsetY = 0;

        @SerializedName("viewAngle")
        private float viewAngle = 90.0F;

        @SerializedName("showCoordinates")
        private boolean showCoordinates = true;

        @SerializedName("elementsOnXpBar")
        private boolean elementsOnXpBar = true;

        @SerializedName("coordinatesFormat")
        private CoordinatesFormat coordinatesFormat = CoordinatesFormat.XYZ;

        @SerializedName("showDays")
        private boolean showDays = false;

        @SerializedName("daysDisplayOrder")
        private DaysDisplayOrder daysDisplayOrder = DaysDisplayOrder.DAYS_UNDER_COORDS;

        @SerializedName("showWorldDirections")
        private boolean showWorldDirections = true;

        @SerializedName("worldDirectionsScale")
        private float worldDirectionsScale = 1.0F;

        // Deprecated fields for migration
        @SerializedName("showPlayerHeads")
        private boolean showPlayerHeads = true;
        @SerializedName("playerHeadsScale")
        private float playerHeadsScale = 1.0F;
        @SerializedName("playerHeadOutline")
        private boolean playerHeadOutline = false;
        // ---

        @SerializedName("playerMarkerType")
        private PlayerMarkerType playerMarkerType = PlayerMarkerType.HEADS;

        @SerializedName("playerMarkersScale")
        private float playerMarkersScale = 1.0F;

        @SerializedName("playerMarkerOutline")
        private boolean playerMarkerOutline = false;

        @SerializedName("playerMarkerFadeStartDistance")
        private float playerMarkerFadeStartDistance = 50.0F;

        @SerializedName("playerMarkerFadeToMinDistance")
        private float playerMarkerFadeToMinDistance = 125.0F;

        @SerializedName("playerMarkerHideDistance")
        private float playerMarkerHideDistance = LocatorBarConfig.INFINITE_PLAYER_HEAD_DISTANCE;

        @SerializedName("playerMarkerMinAlphaPercent")
        private float playerMarkerMinAlphaPercent = 40.0F;

        @SerializedName("maxVisiblePlayers")
        private int maxVisiblePlayers = 16;

        @SerializedName("showWaypoints")
        private boolean showWaypoints = true;

        @SerializedName("showDeathWaypoint")
        private boolean showDeathWaypoint = true;

        @SerializedName("waypointsScale")
        private float waypointsScale = 1.0F;

        @SerializedName("maxVisibleWaypoints")
        private int maxVisibleWaypoints = 16;

        @SerializedName("waypoints")
        private Map<UUID, WaypointConfig> waypoints = new HashMap<>();
    }
}