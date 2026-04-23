package pl.fuzjajadrowa.locatorbar.waypoint;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.LodestoneTracker;

import java.util.UUID;

public final class WaypointData {
    public static final String OWNER_TAG = "locatorbar_waypoint_owner";
    public static final String ID_TAG = "locatorbar_waypoint_id";
    public static final String INDEX_TAG = "locatorbar_waypoint_index";

    private WaypointData() {
    }

    public static void ensureWaypointData(ItemStack stack, Player player) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return;
        }

        CompoundTag tag = getCustomDataTag(stack);
        tag.putUUID(OWNER_TAG, player.getUUID());

        if (!tag.hasUUID(ID_TAG)) {
            tag.putUUID(ID_TAG, UUID.randomUUID());
        }

        if (!tag.contains(INDEX_TAG, Tag.TAG_INT)) {
            tag.putInt(INDEX_TAG, findHighestWaypointIndex(player) + 1);
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static UUID getOwner(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null || !tag.hasUUID(OWNER_TAG)) {
            return null;
        }
        return tag.getUUID(OWNER_TAG);
    }

    public static UUID getWaypointId(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null || !tag.hasUUID(ID_TAG)) {
            return null;
        }
        return tag.getUUID(ID_TAG);
    }

    public static int getWaypointIndex(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null || !tag.contains(INDEX_TAG, Tag.TAG_INT)) {
            return -1;
        }
        return tag.getInt(INDEX_TAG);
    }

    private static int findHighestWaypointIndex(Player player) {
        int highest = 0;
        UUID owner = player.getUUID();

        for (ItemStack stack : player.getInventory().items) {
            highest = Math.max(highest, readWaypointIndexForOwner(stack, owner));
        }
        for (ItemStack stack : player.getInventory().offhand) {
            highest = Math.max(highest, readWaypointIndexForOwner(stack, owner));
        }

        return highest;
    }

    private static int readWaypointIndexForOwner(ItemStack stack, UUID owner) {
        UUID stackOwner = getOwner(stack);
        if (stackOwner == null || !stackOwner.equals(owner)) {
            return 0;
        }

        int index = getWaypointIndex(stack);
        return Math.max(index, 0);
    }

    private static CompoundTag getCustomDataTag(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        return tag == null ? new CompoundTag() : tag;
    }

    private static CompoundTag getCustomDataTagNullable(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        return customData.copyTag();
    }
}