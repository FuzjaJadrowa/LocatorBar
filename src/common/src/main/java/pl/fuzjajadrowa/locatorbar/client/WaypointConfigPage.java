package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig.WaypointConfig;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData.CompassWaypoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class WaypointConfigPage {
    private WaypointConfigPage() {}

    static List<ManagedWaypoint> collectManagedWaypoints() {
        List<ManagedWaypoint> waypoints = new ArrayList<>();
        Set<UUID> seenIds = new HashSet<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return waypoints;
        }

        String currentWorld = mc.player.level().dimension().identifier().toString();

        for (CompassWaypoint compass : ClientWaypointInventory.waypoints(mc.player)) {
            addManagedWaypoint(waypoints, seenIds, compass, currentWorld);
        }

        LocatorBarConfig.getWaypoints().forEach((id, config) -> {
            if (currentWorld.equals(config.world) && !seenIds.contains(id)) {
                waypoints.add(new ManagedWaypoint(id, config.character, config.color, config.visible, config.world, -1));
                seenIds.add(id);
            }
        });

        return waypoints;
    }

    private static void addManagedWaypoint(List<ManagedWaypoint> waypoints, Set<UUID> seenIds, CompassWaypoint compass, String currentWorld) {
        if (compass == null || !compass.dimensionId().equals(currentWorld)) {
            return;
        }

        UUID waypointId = compass.id();
        if (waypointId == null || !seenIds.add(waypointId)) {
            return;
        }

        WaypointConfig config = LocatorBarConfig.getWaypointConfig(waypointId);
        String symbol = config != null ? config.character : compass.symbol();
        Integer customColor = compass.customColor();
        int color = config != null ? config.color : (customColor != null ? customColor : LocatorBarHudHelper.colorFromWaypointId(waypointId));
        boolean visible = config == null ? !compass.hidden() : config.visible;
        int index = compass.index();

        waypoints.add(new ManagedWaypoint(waypointId, symbol, color, visible, currentWorld, index));
    }

    record ManagedWaypoint(UUID id, String symbol, int color, boolean visible, String world, int index) {
    }
}