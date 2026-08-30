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
        if (stack == null || stack.isEmpty()) {
            return;
        }
        //? if >=1.20.5 {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return;
        }
        //?} else {
        /*if (stack.isEmpty() || !stack.getOrCreateTag().contains("LodestonePos")) {
            return;
        }
        *///?}

        CompoundTag tag = getCustomDataTag(stack);
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
            tag.putInt(INDEX_TAG, findHighestWaypointIndex(player) + 1);
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
        CompoundTag tag = getCustomDataTagNullable(stack);
        //? if >=1.21.11
        if (tag == null) {
        //? if <1.21.11
        /*if (tag == null || !tag.hasUUID(OWNER_TAG)) {*/
            return null;
        }
        //? if >=1.21.11
        return getUuid(tag, OWNER_TAG);
        //? if <1.21.11
        /*return tag.getUUID(OWNER_TAG);*/
    }

    public static UUID getWaypointId(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        //? if >=1.21.11
        if (tag == null) {
        //? if <1.21.11
        /*if (tag == null || !tag.hasUUID(ID_TAG)) {*/
            return null;
        }
        //? if >=1.21.11
        return getUuid(tag, ID_TAG);
        //? if <1.21.11
        /*return tag.getUUID(ID_TAG);*/
    }

    public static int getWaypointIndex(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        //? if >=1.21.11
        if (tag == null) {
        //? if <1.21.11
        /*if (tag == null || !tag.contains(INDEX_TAG, Tag.TAG_INT)) {*/
            return -1;
        }
        //? if >=1.21.11
        return tag.getInt(INDEX_TAG).orElse(-1);
        //? if <1.21.11
        /*return tag.getInt(INDEX_TAG);*/
    }

    public static boolean isHidden(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        //? if >=1.21.11
        return tag != null && tag.getBoolean(HIDDEN_TAG).orElse(false);
        //? if <1.21.11
        /*return tag != null && tag.contains(HIDDEN_TAG, Tag.TAG_BYTE) && tag.getBoolean(HIDDEN_TAG);*/
    }

    public static void setHidden(ItemStack stack, boolean hidden) {
        CompoundTag tag = getCustomDataTag(stack);
        if (hidden) {
            tag.putBoolean(HIDDEN_TAG, true);
        } else {
            tag.remove(HIDDEN_TAG);
        }
        //? if >=1.20.5
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static Integer getCustomColor(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        //? if >=1.21.11
        if (tag == null) {
        //? if <1.21.11
        /*if (tag == null || !tag.contains(COLOR_TAG, Tag.TAG_INT)) {*/
            return null;
        }
        //? if >=1.21.11
        return tag.getInt(COLOR_TAG).orElse(null);
        //? if <1.21.11
        /*return tag.getInt(COLOR_TAG);*/
    }

    public static void setCustomColor(ItemStack stack, Integer color) {
        CompoundTag tag = getCustomDataTag(stack);
        if (color == null) {
            tag.remove(COLOR_TAG);
        } else {
            tag.putInt(COLOR_TAG, color);
        }
        //? if >=1.20.5
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static String getWaypointSymbol(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        //? if >=1.21.11
        if (tag == null) {
        //? if <1.21.11
        /*if (tag == null || !tag.contains(SYMBOL_TAG, Tag.TAG_STRING)) {*/
            return null;
        }
        //? if >=1.21.11
        String symbol = tag.getString(SYMBOL_TAG).orElse("");
        //? if <1.21.11
        /*String symbol = tag.getString(SYMBOL_TAG);*/
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
        //? if >=1.20.5
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void unlinkWaypoint(Player player, UUID waypointId) {
        Inventory inventory = player.getInventory();
        //? if >=1.21.11 {
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            clearWaypointDataIfMatches(stack, waypointId);
        }
        clearWaypointDataIfMatches(inventory.getItem(Inventory.SLOT_OFFHAND), waypointId);
        //?} else {
        /*for (ItemStack stack : inventory.items) {
            clearWaypointDataIfMatches(stack, waypointId);
        }
        for (ItemStack stack : inventory.offhand) {
            clearWaypointDataIfMatches(stack, waypointId);
        }
        *///?}
    }

    private static void clearWaypointDataIfMatches(ItemStack stack, UUID waypointId) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (stack.is(net.minecraft.world.item.Items.BUNDLE)) {
            clearWaypointInBundleIfMatches(stack, waypointId);
            return;
        }

        UUID id = getWaypointId(stack);
        if (id != null && id.equals(waypointId)) {
            clearLocatorBarData(stack);
        }
    }

    private static void clearWaypointInBundleIfMatches(ItemStack bundleStack, UUID waypointId) {
        //? if >=1.20.5 {
        net.minecraft.world.item.component.BundleContents bundleContents = bundleStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return;
        }

        java.util.List<ItemStack> updatedItems = new java.util.ArrayList<>();
        boolean modified = false;
        //? if >=1.21.11 {
        for (ItemStack item : bundleContents.items()) {
            ItemStack copy = item.copy();
            if (getWaypointId(copy) != null && getWaypointId(copy).equals(waypointId)) {
                clearLocatorBarData(copy);
                modified = true;
            } else if (copy.is(net.minecraft.world.item.Items.BUNDLE)) {
                clearWaypointInBundleIfMatches(copy, waypointId);
                modified = true;
            }
            updatedItems.add(copy);
        }
        //?} else {
        /*for (ItemStack item : bundleContents.itemCopyStream().toList()) {
            ItemStack copy = item.copy();
            if (getWaypointId(copy) != null && getWaypointId(copy).equals(waypointId)) {
                clearLocatorBarData(copy);
                modified = true;
            } else if (copy.is(net.minecraft.world.item.Items.BUNDLE)) {
                clearWaypointInBundleIfMatches(copy, waypointId);
                modified = true;
            }
            updatedItems.add(copy);
        }
        *///?}

        if (modified) {
            bundleStack.set(DataComponents.BUNDLE_CONTENTS, new net.minecraft.world.item.component.BundleContents(updatedItems));
        }
        //?} else {
        /*CompoundTag tag = bundleStack.getTag();
        if (tag == null || !tag.contains("Items", 9)) {
            return;
        }

        net.minecraft.nbt.ListTag itemsList = tag.getList("Items", 10);
        boolean modified = false;
        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompound(i);
            ItemStack innerStack = ItemStack.of(itemTag);
            if (!innerStack.isEmpty()) {
                if (getWaypointId(innerStack) != null && getWaypointId(innerStack).equals(waypointId)) {
                    clearLocatorBarData(innerStack);
                    itemsList.set(i, innerStack.save(new CompoundTag()));
                    modified = true;
                } else if (innerStack.is(net.minecraft.world.item.Items.BUNDLE)) {
                    clearWaypointInBundleIfMatches(innerStack, waypointId);
                    itemsList.set(i, innerStack.save(new CompoundTag()));
                    modified = true;
                }
            }
        }

        if (modified) {
            tag.put("Items", itemsList);
            bundleStack.setTag(tag);
        }
        *///?}
    }

    private static void clearLocatorBarData(ItemStack stack) {
        CompoundTag tag = getCustomDataTagNullable(stack);
        if (tag == null) {
            return;
        }

        tag.remove(OWNER_TAG);
        tag.remove(ID_TAG);
        tag.remove(INDEX_TAG);
        tag.remove(HIDDEN_TAG);
        tag.remove(COLOR_TAG);
        tag.remove(SYMBOL_TAG);

        //? if >=1.20.5 {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        //?} else {
        /*if (tag.isEmpty()) {
            stack.setTag(null);
        }
        *///?}
    }

    private static int findHighestWaypointIndex(Player player) {
        int highest = 0;
        UUID owner = player.getUUID();

        //? if >=1.21.11 {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            highest = Math.max(highest, readWaypointIndexForOwner(stack, owner));
        }
        ItemStack offhand = player.getInventory().getItem(Inventory.SLOT_OFFHAND);
        if (!offhand.isEmpty()) {
            highest = Math.max(highest, readWaypointIndexForOwner(offhand, owner));
        }
        //?} else {
        /*for (ItemStack stack : player.getInventory().items) {
            highest = Math.max(highest, readWaypointIndexForOwner(stack, owner));
        }
        for (ItemStack stack : player.getInventory().offhand) {
            highest = Math.max(highest, readWaypointIndexForOwner(stack, owner));
        }
        *///?}

        return highest;
    }

    private static int readWaypointIndexForOwner(ItemStack stack, UUID owner) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        if (stack.is(net.minecraft.world.item.Items.BUNDLE)) {
            int[] highest = new int[]{0};
            pl.fuzjajadrowa.locatorbar.util.LocatorBarUtils.forEachBundleItem(stack, inner -> {
                highest[0] = Math.max(highest[0], readWaypointIndexForOwner(inner, owner));
            });
            return highest[0];
        }

        UUID stackOwner = getOwner(stack);
        if (stackOwner == null || !stackOwner.equals(owner)) {
            return 0;
        }

        int index = getWaypointIndex(stack);
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