package pl.fuzjajadrowa.locatorbar.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerConfigData;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerConfigSerializationTest {
    @TempDir
    Path tempDir;

    @Test
    void partialJsonPreservesDefaultServerSettingsFields() throws Exception {
        Path configPath = tempDir.resolve("locatorbar-server.json");
        Files.writeString(configPath, "{\"style\":\"CLASSIC\",\"maxVisiblePlayers\":8}");

        ConfigFile<ServerConfigData> file = new ConfigFile<>(configPath, ServerConfigData.class);
        ServerSettings settings = file.read(ServerConfigData::new).toSettings();

        assertEquals(LocatorBarStyle.CLASSIC, settings.style());
        assertEquals(8, settings.maxVisiblePlayers());
        assertTrue(settings.showCoordinates());
        assertTrue(settings.showWorldDirections());
        assertTrue(settings.showWaypoints());
        assertTrue(settings.showDeathWaypoint());
        assertEquals(PlayerMarkerType.HEADS, settings.playerMarkerType());
        assertEquals(ServerSettings.DEFAULT_PLAYER_MARKER_FADE_START_DISTANCE, settings.playerMarkerFadeStartDistance(), 1.0E-5F);
        assertEquals(ServerSettings.DEFAULT_PLAYER_MARKER_FADE_TO_MIN_DISTANCE, settings.playerMarkerFadeToMinDistance(), 1.0E-5F);
        assertEquals(ServerSettings.DEFAULT_PLAYER_MARKER_HIDE_DISTANCE, settings.playerMarkerHideDistance(), 1.0E-5F);
        assertEquals(ServerSettings.DEFAULT_PLAYER_MARKER_MIN_ALPHA_PERCENT, settings.playerMarkerMinAlphaPercent(), 1.0E-5F);
    }

    @Test
    void enumFromOrdinalValidatesBounds() {
        assertEquals(LocatorBarStyle.REWORKED, LocatorBarStyle.fromOrdinal(-1));
        assertEquals(LocatorBarStyle.REWORKED, LocatorBarStyle.fromOrdinal(99));
        assertEquals(LocatorBarStyle.CLASSIC, LocatorBarStyle.fromOrdinal(LocatorBarStyle.CLASSIC.ordinal()));

        assertEquals(PlayerMarkerType.HEADS, PlayerMarkerType.fromOrdinal(-5));
        assertEquals(PlayerMarkerType.HEADS, PlayerMarkerType.fromOrdinal(42));
        assertEquals(PlayerMarkerType.DOTS, PlayerMarkerType.fromOrdinal(PlayerMarkerType.DOTS.ordinal()));
    }
}