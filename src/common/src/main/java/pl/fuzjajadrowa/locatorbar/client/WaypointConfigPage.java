package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;

//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.LodestoneTracker;
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig.WaypointConfig;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointInventory;

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

        WaypointInventory.forEachLeaf(mc.player,
                stack -> addManagedWaypoint(waypoints, seenIds, stack, currentWorld));

        LocatorBarConfig.getWaypoints().forEach((id, config) -> {
            if (currentWorld.equals(config.world) && !seenIds.contains(id)) {
                waypoints.add(new ManagedWaypoint(id, config.character, config.color, config.visible, config.world, -1));
                seenIds.add(id);
            }
        });

        return waypoints;
    }

    private static void addManagedWaypoint(List<ManagedWaypoint> waypoints, Set<UUID> seenIds, ItemStack stack, String currentWorld) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        //? if >=1.20.5 {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return;
        }

        if (!tracker.target().get().dimension().identifier().toString().equals(currentWorld)) {
            return;
        }
        //?} else {
        /*CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("LodestonePos") || !tag.contains("LodestoneDimension")) {
            return;
        }

        String dimensionStr = tag.getString("LodestoneDimension");
        if (!dimensionStr.equals(currentWorld)) {
            return;
        }
        *///?}

        UUID waypointId = WaypointData.getWaypointId(stack);
        if (waypointId == null) {
            return;
        }

        if (seenIds.contains(waypointId)) {
            return;
        }

        WaypointConfig config = LocatorBarConfig.getWaypointConfig(waypointId);
        String symbol = config != null ? config.character : WaypointData.getWaypointSymbol(stack);
        Integer customColor = WaypointData.getCustomColor(stack);
        int color = config != null ? config.color : (customColor != null ? customColor : LocatorBarHudHelper.colorFromWaypointId(waypointId));
        boolean visible = config == null ? !WaypointData.isHidden(stack) : config.visible;
        int index = WaypointData.getWaypointIndex(stack);

        waypoints.add(new ManagedWaypoint(waypointId, symbol, color, visible, currentWorld, index));
        seenIds.add(waypointId);
    }

    record ManagedWaypoint(UUID id, String symbol, int color, boolean visible, String world, int index) {
    }
}