package pl.fuzjajadrowa.locatorbar.waypoint;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.LodestoneTracker;

import java.util.Optional;
import java.util.UUID;

public final class LodestoneWaypointData {
    private static final String ROOT_TAG = "locatorbar";
    private static final String OWNER_TAG = "owner";
    private static final String WAYPOINT_ID_TAG = "waypoint_id";

    private LodestoneWaypointData() {
    }

    public static Optional<WaypointInfo> getOwnedWaypoint(ItemStack stack, UUID playerId) {
        return getWaypointInfo(stack).filter(info -> info.owner().equals(playerId));
    }

    public static void assignWaypointDataIfNeeded(ItemStack stack, ServerPlayer player) {
        if (!hasLodestoneTarget(stack)) {
            return;
        }

        UUID playerId = player.getUUID();
        Optional<WaypointInfo> existing = getWaypointInfo(stack);
        if (existing.isPresent() && existing.get().owner().equals(playerId) && existing.get().id() > 0) {
            return;
        }

        int nextId = getNextWaypointId(player);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag locatorTag = root.contains(ROOT_TAG, Tag.TAG_COMPOUND) ? root.getCompound(ROOT_TAG) : new CompoundTag();
            locatorTag.putUUID(OWNER_TAG, playerId);
            locatorTag.putInt(WAYPOINT_ID_TAG, nextId);
            root.put(ROOT_TAG, locatorTag);
        });
    }

    private static boolean hasLodestoneTarget(ItemStack stack) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        return tracker != null && tracker.target().isPresent();
    }

    private static int getNextWaypointId(Player player) {
        int maxId = 0;
        UUID playerId = player.getUUID();
        Inventory inventory = player.getInventory();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack inventoryStack = inventory.getItem(slot);
            Optional<WaypointInfo> info = getOwnedWaypoint(inventoryStack, playerId);
            if (info.isPresent()) {
                maxId = Math.max(maxId, info.get().id());
            }
        }

        return maxId + 1;
    }

    private static Optional<WaypointInfo> getWaypointInfo(ItemStack stack) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return Optional.empty();
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.isEmpty()) {
            return Optional.empty();
        }

        CompoundTag root = customData.copyTag();
        if (!root.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        CompoundTag locatorTag = root.getCompound(ROOT_TAG);
        if (!locatorTag.hasUUID(OWNER_TAG) || !locatorTag.contains(WAYPOINT_ID_TAG, Tag.TAG_INT)) {
            return Optional.empty();
        }

        int waypointId = locatorTag.getInt(WAYPOINT_ID_TAG);
        if (waypointId <= 0) {
            return Optional.empty();
        }

        UUID owner = locatorTag.getUUID(OWNER_TAG);
        GlobalPos target = tracker.target().get();
        return Optional.of(new WaypointInfo(target, owner, waypointId));
    }

    public record WaypointInfo(GlobalPos target, UUID owner, int id) {
    }
}