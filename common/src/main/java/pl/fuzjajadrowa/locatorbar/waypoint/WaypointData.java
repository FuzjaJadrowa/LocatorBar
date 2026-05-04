package pl.fuzjajadrowa.locatorbar.waypoint;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.LodestoneTracker;

import java.util.UUID;

public final class WaypointData {
    public static final String OWNER_TAG = "locatorbar_waypoint_owner";
    public static final String ID_TAG = "locatorbar_waypoint_id";
    public static final String INDEX_TAG = "locatorbar_waypoint_index";
    public static final String HIDDEN_TAG = "locatorbar_waypoint_hidden";
    public static final String COLOR_TAG = "locatorbar_waypoint_color";
    public static final String SYMBOL_TAG = "locatorbar_waypoint_symbol";

    private WaypointData() {
    }

    public static void ensureWaypointData(ItemStack stack, Player player) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return;
        }

        CompoundTag tag = getCustomDataTag(stack);
        putUuid(tag, OWNER_TAG, player.getUUID());

        if (!hasUuid(tag, ID_TAG)) {
            putUuid(tag, ID_TAG, UUID.randomUUID());
        }

        if (!tag.contains(INDEX_TAG)) {
            tag.putInt(INDEX_TAG, findHighestWaypointIndex(player) + 1);
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static UUID getOwner(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null) {
            return null;
        }
        return getUuid(tag, OWNER_TAG);
    }

    public static UUID getWaypointId(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null) {
            return null;
        }
        return getUuid(tag, ID_TAG);
    }

    public static int getWaypointIndex(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null) {
            return -1;
        }
        return tag.getInt(INDEX_TAG).orElse(-1);
    }

    public static boolean isHidden(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        return tag != null && tag.getBoolean(HIDDEN_TAG).orElse(false);
    }

    public static void setHidden(ItemStack stack, boolean hidden) {
        CompoundTag tag = getCustomDataTag(stack);
        if (hidden) {
            tag.putBoolean(HIDDEN_TAG, true);
        } else {
            tag.remove(HIDDEN_TAG);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static Integer getCustomColor(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null) {
            return null;
        }
        return tag.getInt(COLOR_TAG).orElse(null);
    }

    public static void setCustomColor(ItemStack stack, Integer color) {
        CompoundTag tag = getCustomDataTag(stack);
        if (color == null) {
            tag.remove(COLOR_TAG);
        } else {
            tag.putInt(COLOR_TAG, color);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static String getWaypointSymbol(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null) {
            return null;
        }
        String symbol = tag.getString(SYMBOL_TAG).orElse("");
        if (symbol.isEmpty()) {
            return null;
        }
        return symbol.substring(0, 1);
    }

    public static void setWaypointSymbol(ItemStack stack, String symbol) {
        CompoundTag tag = getCustomDataTag(stack);
        if (symbol == null || symbol.isEmpty()) {
            tag.remove(SYMBOL_TAG);
        } else {
            tag.putString(SYMBOL_TAG, symbol.substring(0, 1));
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static int findHighestWaypointIndex(Player player) {
        int highest = 0;
        UUID owner = player.getUUID();

        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            highest = Math.max(highest, readWaypointIndexForOwner(stack, owner));
        }
        ItemStack offhand = player.getInventory().getItem(Inventory.SLOT_OFFHAND);
        if (!offhand.isEmpty()) {
            highest = Math.max(highest, readWaypointIndexForOwner(offhand, owner));
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

    private static void putUuid(CompoundTag tag, String key, UUID uuid) {
        tag.putIntArray(key, uuidToIntArray(uuid));
    }

    private static boolean hasUuid(CompoundTag tag, String key) {
        return tag.getIntArray(key).map(values -> values.length == 4).orElse(false);
    }

    private static UUID getUuid(CompoundTag tag, String key) {
        return tag.getIntArray(key).filter(values -> values.length == 4).map(WaypointData::intArrayToUuid).orElse(null);
    }

    private static int[] uuidToIntArray(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        return new int[]{
                (int) (most >> 32),
                (int) most,
                (int) (least >> 32),
                (int) least
        };
    }

    private static UUID intArrayToUuid(int[] values) {
        long most = ((long) values[0] << 32) | (values[1] & 0xFFFFFFFFL);
        long least = ((long) values[2] << 32) | (values[3] & 0xFFFFFFFFL);
        return new UUID(most, least);
    }
}