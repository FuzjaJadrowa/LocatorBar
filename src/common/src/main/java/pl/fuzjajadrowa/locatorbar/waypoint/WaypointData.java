package pl.fuzjajadrowa.locatorbar.waypoint;

//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
//?}
import net.minecraft.nbt.CompoundTag;
//? if <1.21.11
/*import net.minecraft.nbt.Tag;*/
import net.minecraft.core.GlobalPos;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//? if >=1.20.5 {
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.LodestoneTracker;
//?}

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class WaypointData {
    public static final String OWNER_TAG = "locatorbar_waypoint_owner";
    public static final String ID_TAG = "locatorbar_waypoint_id";
    public static final String INDEX_TAG = "locatorbar_waypoint_index";
    public static final String HIDDEN_TAG = "locatorbar_waypoint_hidden";
    public static final String COLOR_TAG = "locatorbar_waypoint_color";
    public static final String SYMBOL_TAG = "locatorbar_waypoint_symbol";
    public static final String UNLINKED_TAG = "locatorbar_waypoint_unlinked";

    public record CompassWaypoint(
            UUID id,
            String dimensionId,
            BlockPos pos,
            int index,
            Integer customColor,
            String symbol,
            boolean hidden
    ) {
    }

    private WaypointData() {
    }

    public static UUID deriveDeterministicId(String dimensionId, BlockPos pos) {
        String key = "locatorbar:lodestone:" + dimensionId + ":" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    public static CompassWaypoint readCompass(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        String dimensionId;
        BlockPos targetPos;
        //? if >=1.20.5 {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return null;
        }
        GlobalPos target = tracker.target().get();
        dimensionId = target.dimension().identifier().toString();
        targetPos = target.pos();
        //?} else {
        /*CompoundTag stackTag = stack.getTag();
        if (stackTag == null || !stackTag.contains("LodestonePos") || !stackTag.contains("LodestoneDimension")) {
            return null;
        }
        CompoundTag posTag = stackTag.getCompound("LodestonePos");
        targetPos = net.minecraft.nbt.NbtUtils.readBlockPos(posTag);
        dimensionId = stackTag.getString("LodestoneDimension");
        *///?}

        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag != null && readBoolean(tag, UNLINKED_TAG)) {
            return null;
        }

        UUID id = readUuid(tag, ID_TAG);
        if (id == null) {
            id = deriveDeterministicId(dimensionId, targetPos);
        }

        int index = readInt(tag, INDEX_TAG, -1);
        Integer customColor = readNullableInt(tag, COLOR_TAG);
        String symbol = readSymbol(tag);
        boolean hidden = readBoolean(tag, HIDDEN_TAG);

        return new CompassWaypoint(id, dimensionId, targetPos, index, customColor, symbol, hidden);
    }

    public static void ensureWaypointData(ItemStack stack, Player player) {
        ensureWaypointData(stack, player, () -> findHighestWaypointIndex(player) + 1);
    }

    static void ensureWaypointData(ItemStack stack, Player player, java.util.function.IntSupplier nextIndex) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        //? if >=1.20.5 {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return;
        }
        //?} else {
        /*if (stack.getTag() == null || !stack.getTag().contains("LodestonePos")) {
            return;
        }
        *///?}

        CompoundTag tag = getCustomDataTag(stack);
        tag.remove(UNLINKED_TAG);
        //? if >=1.21.11
        putUuid(tag, OWNER_TAG, player.getUUID());
        //? if <1.21.11
        /*tag.putUUID(OWNER_TAG, player.getUUID());*/

        //? if >=1.21.11 {
        if (!hasUuid(tag, ID_TAG)) {
            putUuid(tag, ID_TAG, UUID.randomUUID());
        }
        //?} else {
        /*if (!tag.hasUUID(ID_TAG)) {
            tag.putUUID(ID_TAG, UUID.randomUUID());
        }
        *///?}

        //? if >=1.21.11
        if (!tag.contains(INDEX_TAG)) {
        //? if <1.21.11
        /*if (!tag.contains(INDEX_TAG, Tag.TAG_INT)) {*/
            tag.putInt(INDEX_TAG, nextIndex.getAsInt());
        }

        //? if >=1.20.5
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean ensureWaypointData(ItemStack stack, Player player, ResourceKey<Level> dimension, BlockPos pos) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        //? if >=1.20.5 {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return false;
        }

        GlobalPos target = tracker.target().get();
        if (!target.dimension().equals(dimension) || !target.pos().equals(pos)) {
            return false;
        }
        //?} else {
        /*CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("LodestonePos") || !tag.contains("LodestoneDimension")) {
            return false;
        }
        CompoundTag posTag = tag.getCompound("LodestonePos");
        BlockPos targetPos = net.minecraft.nbt.NbtUtils.readBlockPos(posTag);
        String dimensionStr = tag.getString("LodestoneDimension");
        if (!dimensionStr.equals(dimension.location().toString()) || !targetPos.equals(pos)) {
            return false;
        }
        *///?}

        ensureWaypointData(stack, player);
        return getWaypointId(stack) != null;
    }

    public static boolean ensureClickedLodestoneWaypoint(Player player, ResourceKey<Level> dimension, BlockPos pos) {
        ItemStack mainHand = player.getMainHandItem();
        if (ensureWaypointData(mainHand, player, dimension, pos)) {
            return true;
        }

        ItemStack offhand = player.getOffhandItem();
        if (ensureWaypointData(offhand, player, dimension, pos)) {
            return true;
        }

        Inventory inventory = player.getInventory();
        //? if >=1.21.11 {
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            if (ensureWaypointData(stack, player, dimension, pos)) {
                return true;
            }
        }
        //?} else {
        /*for (ItemStack stack : inventory.items) {
            if (ensureWaypointData(stack, player, dimension, pos)) {
                return true;
            }
        }
        *///?}

        return false;
    }

    public static UUID getOwner(ItemStack stack) {
        return readUuid(getCustomDataTagNullable(stack), OWNER_TAG);
    }

    public static UUID getWaypointId(ItemStack stack) {
        return readUuid(getCustomDataTagNullable(stack), ID_TAG);
    }

    public static UUID getOrDeriveWaypointId(ItemStack stack) {
        CompassWaypoint compass = readCompass(stack);
        return compass == null ? null : compass.id();
    }

    public static boolean isUnlinked(ItemStack stack) {
        return readBoolean(getCustomDataTagNullable(stack), UNLINKED_TAG);
    }

    public static int getWaypointIndex(ItemStack stack) {
        return readInt(getCustomDataTagNullable(stack), INDEX_TAG, -1);
    }

    public static boolean isHidden(ItemStack stack) {
        return readBoolean(getCustomDataTagNullable(stack), HIDDEN_TAG);
    }

    public static void setHidden(ItemStack stack, boolean hidden) {
        CompoundTag tag = getCustomDataTag(stack);
        if (hidden) {
            tag.putBoolean(HIDDEN_TAG, true);
        } else {
            tag.remove(HIDDEN_TAG);
        }
        //? if >=1.20.5 {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        //?}
    }

    public static Integer getCustomColor(ItemStack stack) {
        return readNullableInt(getCustomDataTagNullable(stack), COLOR_TAG);
    }

    public static void setCustomColor(ItemStack stack, Integer color) {
        CompoundTag tag = getCustomDataTag(stack);
        if (color == null) {
            tag.remove(COLOR_TAG);
        } else {
            tag.putInt(COLOR_TAG, color);
        }
        //? if >=1.20.5 {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        //?}
    }

    public static String getWaypointSymbol(ItemStack stack) {
        return readSymbol(getCustomDataTagNullable(stack));
    }

    public static void setWaypointSymbol(ItemStack stack, String symbol) {
        CompoundTag tag = getCustomDataTag(stack);
        if (symbol == null || symbol.isEmpty()) {
            tag.remove(SYMBOL_TAG);
        } else {
            tag.putString(SYMBOL_TAG, symbol.substring(0, Character.charCount(symbol.codePointAt(0))));
        }
        //? if >=1.20.5 {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        //?}
    }

    public static void unlinkWaypoint(Player player, UUID waypointId) {
        WaypointInventory.unlink(player, waypointId);
    }

    static void clearLocatorBarData(ItemStack stack) {
        CompoundTag tag = getCustomDataTag(stack);

        tag.remove(OWNER_TAG);
        tag.remove(ID_TAG);
        tag.remove(INDEX_TAG);
        tag.remove(HIDDEN_TAG);
        tag.remove(COLOR_TAG);
        tag.remove(SYMBOL_TAG);
        tag.putBoolean(UNLINKED_TAG, true);

        //? if >=1.20.5 {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        //?} else {
        /*stack.setTag(tag);
        *///?}
    }

    private static UUID readUuid(CompoundTag tag, String key) {
        //? if >=1.21.11 {
        return tag == null ? null : getUuid(tag, key);
        //?} else {
        /*return tag != null && tag.hasUUID(key) ? tag.getUUID(key) : null;
        *///?}
    }

    private static int readInt(CompoundTag tag, String key, int fallback) {
        //? if >=1.21.11 {
        return tag == null ? fallback : tag.getInt(key).orElse(fallback);
        //?} else {
        /*return tag != null && tag.contains(key, Tag.TAG_INT) ? tag.getInt(key) : fallback;
        *///?}
    }

    private static Integer readNullableInt(CompoundTag tag, String key) {
        //? if >=1.21.11 {
        return tag == null ? null : tag.getInt(key).orElse(null);
        //?} else {
        /*return tag != null && tag.contains(key, Tag.TAG_INT) ? tag.getInt(key) : null;
        *///?}
    }

    private static boolean readBoolean(CompoundTag tag, String key) {
        //? if >=1.21.11 {
        return tag != null && tag.getBoolean(key).orElse(false);
        //?} else {
        /*return tag != null && tag.contains(key, Tag.TAG_BYTE) && tag.getBoolean(key);
        *///?}
    }

    private static String readSymbol(CompoundTag tag) {
        //? if >=1.21.11 {
        if (tag == null) {
            return null;
        }
        String symbol = tag.getString(SYMBOL_TAG).orElse("");
        //?} else {
        /*if (tag == null || !tag.contains(SYMBOL_TAG, Tag.TAG_STRING)) {
            return null;
        }
        String symbol = tag.getString(SYMBOL_TAG);
        *///?}
        if (symbol.isEmpty()) {
            return null;
        }
        return symbol.substring(0, Character.charCount(symbol.codePointAt(0)));
    }

    static int findHighestWaypointIndex(Player player) {
        int[] highest = {0};
        UUID owner = player.getUUID();
        WaypointInventory.forEachLeaf(player, stack ->
                highest[0] = Math.max(highest[0], readWaypointIndexForOwner(stack, owner)));
        return highest[0];
    }

    private static int readWaypointIndexForOwner(ItemStack stack, UUID owner) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null) {
            return 0;
        }
        UUID stackOwner = readUuid(tag, OWNER_TAG);
        if (stackOwner == null || !stackOwner.equals(owner)) {
            return 0;
        }
        int index = readInt(tag, INDEX_TAG, -1);
        return Math.max(index, 0);
    }

    private static CompoundTag getCustomDataTag(ItemStack stack) {
        //? if >=1.20.5 {
        CompoundTag tag = getCustomDataTagNullable(stack);
        return tag == null ? new CompoundTag() : tag;
        //?} else {
        /*return stack.getOrCreateTag();
        *///?}
    }

    private static CompoundTag getCustomDataTagNullable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        //? if >=1.20.5 {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        return customData.copyTag();
        //?} else {
        /*return stack.getTag();
        *///?}
    }

    //? if >=1.21.11 {
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
    //?}
}