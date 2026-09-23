package pl.fuzjajadrowa.locatorbar.waypoint;

//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class WaypointInventory {
    private WaypointInventory() {}

    public static void forEachStack(Player player, java.util.function.Consumer<ItemStack> consumer) {
        //? if >=1.21.11 {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) consumer.accept(stack);
        consumer.accept(player.getInventory().getItem(Inventory.SLOT_OFFHAND));
        //?} else {
        /*for (ItemStack stack : player.getInventory().items) consumer.accept(stack);
        for (ItemStack stack : player.getInventory().offhand) consumer.accept(stack);
        *///?}
    }

    public static void forEachLeaf(Player player, java.util.function.Consumer<ItemStack> consumer) {
        forEachStack(player, stack -> {
            if (stack.is(net.minecraft.world.item.Items.BUNDLE)) {
                pl.fuzjajadrowa.locatorbar.util.LocatorBarUtils.forEachBundleItem(stack, consumer);
            } else if (!stack.isEmpty()) {
                consumer.accept(stack);
            }
        });
    }

    public static void migrate(Player player) {
        int[] highest = {-1};
        forEachStack(player, stack -> updateStack(stack, item -> {
            if (WaypointData.getWaypointId(item) != null) return false;
            WaypointData.ensureWaypointData(item, player, () -> {
                if (highest[0] < 0) highest[0] = WaypointData.findHighestWaypointIndex(player);
                return ++highest[0];
            });
            return WaypointData.getWaypointId(item) != null;
        }));
    }

    public static void unlink(Player player, UUID waypointId) {
        forEachStack(player, stack -> updateStack(stack, item -> {
            UUID id = WaypointData.getWaypointId(item);
            if (!waypointId.equals(id)) return false;
            WaypointData.clearLocatorBarData(item);
            return true;
        }));
    }

    private static boolean updateStack(ItemStack stack, java.util.function.Predicate<ItemStack> update) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(net.minecraft.world.item.Items.BUNDLE)) return updateBundle(stack, update);
        return update.test(stack);
    }

    private static boolean updateBundle(ItemStack bundleStack, java.util.function.Predicate<ItemStack> update) {
        //? if >=1.20.5 {
        net.minecraft.world.item.component.BundleContents bundleContents = bundleStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return false;
        }

        java.util.List<ItemStack> updatedItems = new java.util.ArrayList<>();
        boolean modified = false;
        //? if >=26.3 {
        java.util.Iterator<ItemStack> items = bundleContents.itemCopies().iterator();
        //?} else {
        /*java.util.Iterator<ItemStack> items = bundleContents.itemCopyStream().iterator();
        *///?}
        while (items.hasNext()) {
            ItemStack copy = items.next();
            modified |= updateStack(copy, update);
            updatedItems.add(copy);
        }

        if (modified) {
            //? if >=26.1 {
            /*bundleStack.set(DataComponents.BUNDLE_CONTENTS, new net.minecraft.world.item.component.BundleContents(
                    updatedItems.stream().map(st -> new net.minecraft.world.item.ItemStackTemplate(st.getItem().builtInRegistryHolder(), st.getCount(), st.getComponentsPatch())).toList()
            ));*/
            //?} else {
            bundleStack.set(DataComponents.BUNDLE_CONTENTS, new net.minecraft.world.item.component.BundleContents(updatedItems));
            //?}
        }
        return modified;
        //?} else {
        /*CompoundTag tag = bundleStack.getTag();
        if (tag == null || !tag.contains("Items", 9)) {
            return false;
        }

        net.minecraft.nbt.ListTag itemsList = tag.getList("Items", 10);
        boolean modified = false;
        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompound(i);
            ItemStack innerStack = ItemStack.of(itemTag);
            if (!innerStack.isEmpty()) {
                if (updateStack(innerStack, update)) {
                    itemsList.set(i, innerStack.save(new CompoundTag()));
                    modified = true;
                }
            }
        }

        if (modified) {
            tag.put("Items", itemsList);
            bundleStack.setTag(tag);
        }
        return modified;
        *///?}
    }
}
