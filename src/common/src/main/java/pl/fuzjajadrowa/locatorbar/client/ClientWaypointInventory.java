package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData.CompassWaypoint;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointInventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ClientWaypointInventory {
    private static Player owner;
    private static Object level;
    private static List<CompassWaypoint> waypoints = List.of();
    private static boolean recoveryCompass;
    private static final Set<UUID> unlinkedIds = new HashSet<>();

    private ClientWaypointInventory() {}

    public static void tick(Player player) {
        if (player == null) {
            owner = null;
            level = null;
            waypoints = List.of();
            recoveryCompass = false;
            unlinkedIds.clear();
            return;
        }
        owner = player;
        level = player.level();
        recoveryCompass = false;
        if (!LocatorBarConfig.isEnabled() || !LocatorBarConfig.isShowWaypoints()) {
            waypoints = List.of();
            return;
        }
        List<CompassWaypoint> snapshot = new ArrayList<>();
        WaypointInventory.forEachLeaf(player, stack -> {
            if (stack.is(Items.RECOVERY_COMPASS)) {
                recoveryCompass = true;
            }
            CompassWaypoint compass = WaypointData.readCompass(stack);
            if (compass != null && !unlinkedIds.contains(compass.id())) {
                snapshot.add(compass);
            }
        });
        waypoints = List.copyOf(snapshot);
    }

    public static void unlink(Player player, UUID waypointId) {
        if (waypointId == null) {
            return;
        }
        unlinkedIds.add(waypointId);
        if (player != null) {
            WaypointData.unlinkWaypoint(player, waypointId);
        }
        LocatorBar.sendUnlinkWaypoint(waypointId);
        if (player != null) {
            tick(player);
        }
    }

    public static void onLodestoneLinked(UUID waypointId) {
        if (waypointId != null) {
            unlinkedIds.remove(waypointId);
        }
    }

    static List<CompassWaypoint> waypoints(Player player) {
        return owner == player && level == player.level() ? waypoints : List.of();
    }

    static boolean hasRecoveryCompass(Player player) {
        return owner == player && level == player.level() && recoveryCompass;
    }
}