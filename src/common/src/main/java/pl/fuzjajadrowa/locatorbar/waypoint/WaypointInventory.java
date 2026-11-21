package pl.fuzjajadrowa.locatorbar.waypoint;

//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
//?}
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData.CompassWaypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class WaypointInventory {
    private static final int MAX_RECURSION_DEPTH = 6;
    private static volatile BiConsumer<ItemStack, Consumer<ItemStack>> platformContainerReader;
    private static volatile BiPredicate<ItemStack, Predicate<ItemStack>> platformContainerUpdater;

    private WaypointInventory() {}

    public static void registerPlatformContainerSupport(
            BiConsumer<ItemStack, Consumer<ItemStack>> reader,
            BiPredicate<ItemStack, Predicate<ItemStack>> updater
    ) {
        platformContainerReader = reader;
        platformContainerUpdater = updater;
    }

    public static void forEachStack(Player player, Consumer<ItemStack> consumer) {
        Set<ItemStack> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Consumer<ItemStack> deduped = stack -> {
            if (stack != null && !stack.isEmpty() && visited.add(stack)) {
                consumer.accept(stack);
            }
        };

        //? if >=1.21.11 {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) deduped.accept(stack);
        deduped.accept(player.getInventory().getItem(Inventory.SLOT_OFFHAND));
        //?} else {
        /*for (ItemStack stack : player.getInventory().items) deduped.accept(stack);
        for (ItemStack stack : player.getInventory().offhand) deduped.accept(stack);
        for (ItemStack stack : player.getInventory().armor) deduped.accept(stack);
        *///?}

        //? if >=1.20.5 {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                deduped.accept(player.getItemBySlot(slot));
            }
        }
        //?}

        if (player.containerMenu != null) {
            for (Slot slot : player.containerMenu.slots) {
                if (slot != null && slot.hasItem()) {
                    deduped.accept(slot.getItem());
                }
            }
            deduped.accept(player.containerMenu.getCarried());
        }
    }

    public static void forEachLeaf(Player player, Consumer<ItemStack> consumer) {
        forEachLeaf(player, consumer, null);
    }

    public static void forEachLeaf(Player player, Consumer<ItemStack> stackConsumer, Consumer<CompoundTag> nbtConsumer) {
        forEachStack(player, stack -> visitStackRecursive(stack, stackConsumer, nbtConsumer, 0));
    }

    private static void visitStackRecursive(
            ItemStack stack,
            Consumer<ItemStack> stackConsumer,
            Consumer<CompoundTag> nbtConsumer,
            int depth
    ) {
        if (stack == null || stack.isEmpty() || depth > MAX_RECURSION_DEPTH) {
            return;
        }

        stackConsumer.accept(stack);

        //? if >=1.20.5 {
        BundleContents bundleContents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents != null) {
            //? if >=26.3 {
            Iterator<ItemStack> items = bundleContents.itemCopies().iterator();
            //?} else {
            /*Iterator<ItemStack> items = bundleContents.itemCopyStream().iterator();
            *///?}
            while (items.hasNext()) {
                visitStackRecursive(items.next(), stackConsumer, nbtConsumer, depth + 1);
            }
        }

        ItemContainerContents containerContents = stack.get(DataComponents.CONTAINER);
        if (containerContents != null) {
            //? if >=26.1 {
            Iterator<ItemStack> items = containerContents.nonEmptyItemCopyStream().iterator();
            //?} else {
            /*Iterator<ItemStack> items = containerContents.nonEmptyStream().iterator();
            *///?}
            while (items.hasNext()) {
                visitStackRecursive(items.next(), stackConsumer, nbtConsumer, depth + 1);
            }
        }

        if (nbtConsumer != null) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null && !customData.isEmpty()) {
                scanNbtTree(customData.copyTag(), nbtConsumer, depth + 1);
            }
        }
        //?} else {
        /*CompoundTag tag = stack.getTag();
        if (tag != null && !tag.isEmpty()) {
            scanLegacyNbtTree(tag, stackConsumer, nbtConsumer, depth + 1);
        }
        *///?}

        BiConsumer<ItemStack, Consumer<ItemStack>> platformReader = platformContainerReader;
        if (platformReader != null) {
            platformReader.accept(stack, inner -> visitStackRecursive(inner, stackConsumer, nbtConsumer, depth + 1));
        }
    }

    private static void scanNbtTree(CompoundTag tag, Consumer<CompoundTag> nbtConsumer, int depth) {
        if (tag == null || tag.isEmpty() || depth > MAX_RECURSION_DEPTH + 4) {
            return;
        }
        nbtConsumer.accept(tag);
        //? if >=1.21.11 {
        for (String key : tag.keySet()) {
            Tag child = tag.get(key);
            if (child instanceof CompoundTag childCompound) {
                scanNbtTree(childCompound, nbtConsumer, depth + 1);
            } else if (child instanceof ListTag childList) {
                scanNbtList(childList, nbtConsumer, depth + 1);
            }
        }
        //?} else {
        /*for (String key : tag.getAllKeys()) {
            Tag child = tag.get(key);
            if (child instanceof CompoundTag childCompound) {
                scanNbtTree(childCompound, nbtConsumer, depth + 1);
            } else if (child instanceof ListTag childList) {
                scanNbtList(childList, nbtConsumer, depth + 1);
            }
        }
        *///?}
    }

    private static void scanNbtList(ListTag list, Consumer<CompoundTag> nbtConsumer, int depth) {
        if (list == null || list.isEmpty() || depth > MAX_RECURSION_DEPTH + 4) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Tag element = list.get(i);
            if (element instanceof CompoundTag compoundElement) {
                scanNbtTree(compoundElement, nbtConsumer, depth + 1);
            } else if (element instanceof ListTag listElement) {
                scanNbtList(listElement, nbtConsumer, depth + 1);
            }
        }
    }

    //? if <1.20.5 {
    /*private static void scanLegacyNbtTree(
            CompoundTag tag,
            Consumer<ItemStack> stackConsumer,
            Consumer<CompoundTag> nbtConsumer,
            int depth
    ) {
        if (tag == null || tag.isEmpty() || depth > MAX_RECURSION_DEPTH + 4) {
            return;
        }
        if (nbtConsumer != null) {
            nbtConsumer.accept(tag);
        }
        if ((tag.contains("id", 8) || tag.contains("Item", 8)) && (tag.contains("Count") || tag.contains("count"))) {
            ItemStack parsed = ItemStack.of(tag);
            if (!parsed.isEmpty()) {
                visitStackRecursive(parsed, stackConsumer, nbtConsumer, depth + 1);
                return;
            }
        }
        for (String key : tag.getAllKeys()) {
            Tag child = tag.get(key);
            if (child instanceof CompoundTag childCompound) {
                scanLegacyNbtTree(childCompound, stackConsumer, nbtConsumer, depth + 1);
            } else if (child instanceof ListTag childList) {
                for (int i = 0; i < childList.size(); i++) {
                    Tag el = childList.get(i);
                    if (el instanceof CompoundTag compoundEl) {
                        scanLegacyNbtTree(compoundEl, stackConsumer, nbtConsumer, depth + 1);
                    }
                }
            }
        }
    }
    *///?}

    public static void migrate(Player player) {
        int[] highest = {-1};
        forEachStack(player, stack -> updateStack(stack, item -> {
            if (WaypointData.isUnlinked(item) || WaypointData.getWaypointId(item) != null) return false;
            WaypointData.ensureWaypointData(item, player, () -> {
                if (highest[0] < 0) highest[0] = WaypointData.findHighestWaypointIndex(player);
                return ++highest[0];
            });
            return WaypointData.getWaypointId(item) != null;
        }, null, 0));
    }

    public static void unlink(Player player, UUID waypointId) {
        forEachStack(player, stack -> updateStack(stack, item -> {
            UUID id = WaypointData.getOrDeriveWaypointId(item);
            if (!waypointId.equals(id)) return false;
            WaypointData.clearLocatorBarData(item);
            return true;
        }, waypointId, 0));
    }

    private static boolean updateStack(ItemStack stack, Predicate<ItemStack> update, UUID unlinkWaypointId, int depth) {
        if (stack == null || stack.isEmpty() || depth > MAX_RECURSION_DEPTH) return false;
        boolean modified = update.test(stack);

        //? if >=1.20.5 {
        modified |= updateBundle(stack, inner -> updateStack(inner, update, unlinkWaypointId, depth + 1));
        modified |= updateContainerComponent(stack, inner -> updateStack(inner, update, unlinkWaypointId, depth + 1));
        if (unlinkWaypointId != null) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null && !customData.isEmpty()) {
                CompoundTag copy = customData.copyTag();
                if (updateNbtTree(copy, unlinkWaypointId, depth + 1)) {
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(copy));
                    modified = true;
                }
            }
        }
        //?} else {
        /*CompoundTag tag = stack.getTag();
        if (tag != null && !tag.isEmpty()) {
            if (updateLegacyNbtTree(tag, inner -> updateStack(inner, update, unlinkWaypointId, depth + 1), unlinkWaypointId, depth + 1)) {
                stack.setTag(tag);
                modified = true;
            }
        }
        *///?}

        BiPredicate<ItemStack, Predicate<ItemStack>> platformUpdater = platformContainerUpdater;
        if (platformUpdater != null) {
            modified |= platformUpdater.test(stack, inner -> updateStack(inner, update, unlinkWaypointId, depth + 1));
        }
        return modified;
    }

    //? if >=1.20.5 {
    private static boolean updateContainerComponent(ItemStack containerStack, Predicate<ItemStack> update) {
        ItemContainerContents contents = containerStack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return false;
        }
        List<ItemStack> updatedItems = new ArrayList<>();
        boolean modified = false;
        //? if >=26.3 {
        Iterator<ItemStack> items = contents.itemCopies().iterator();
        //?} elif >=26.1 {
        /*Iterator<ItemStack> items = contents.allItemsCopyStream().iterator();
        *///?} else {
        /*Iterator<ItemStack> items = contents.stream().iterator();
        *///?}
        while (items.hasNext()) {
            ItemStack copy = items.next();
            if (!copy.isEmpty()) {
                modified |= update.test(copy);
            }
            updatedItems.add(copy);
        }
        if (modified) {
            containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(updatedItems));
        }
        return modified;
    }
    //?}

    private static boolean updateNbtTree(CompoundTag tag, UUID unlinkWaypointId, int depth) {
        if (tag == null || tag.isEmpty() || depth > MAX_RECURSION_DEPTH + 4) {
            return false;
        }
        boolean modified = WaypointData.updateCompassInTag(tag, unlinkWaypointId);
        //? if >=1.21.11 {
        for (String key : tag.keySet()) {
            Tag child = tag.get(key);
            if (child instanceof CompoundTag childCompound) {
                modified |= updateNbtTree(childCompound, unlinkWaypointId, depth + 1);
            } else if (child instanceof ListTag childList) {
                modified |= updateNbtList(childList, unlinkWaypointId, depth + 1);
            }
        }
        //?} else {
        /*for (String key : tag.getAllKeys()) {
            Tag child = tag.get(key);
            if (child instanceof CompoundTag childCompound) {
                modified |= updateNbtTree(childCompound, unlinkWaypointId, depth + 1);
            } else if (child instanceof ListTag childList) {
                modified |= updateNbtList(childList, unlinkWaypointId, depth + 1);
            }
        }
        *///?}
        return modified;
    }

    private static boolean updateNbtList(ListTag list, UUID unlinkWaypointId, int depth) {
        if (list == null || list.isEmpty() || depth > MAX_RECURSION_DEPTH + 4) {
            return false;
        }
        boolean modified = false;
        for (int i = 0; i < list.size(); i++) {
            Tag element = list.get(i);
            if (element instanceof CompoundTag compoundElement) {
                modified |= updateNbtTree(compoundElement, unlinkWaypointId, depth + 1);
            } else if (element instanceof ListTag listElement) {
                modified |= updateNbtList(listElement, unlinkWaypointId, depth + 1);
            }
        }
        return modified;
    }

    //? if <1.20.5 {
    /*private static boolean updateLegacyNbtTree(
            CompoundTag tag,
            Predicate<ItemStack> stackUpdater,
            UUID unlinkWaypointId,
            int depth
    ) {
        if (tag == null || tag.isEmpty() || depth > MAX_RECURSION_DEPTH + 4) {
            return false;
        }
        boolean modified = false;
        if (unlinkWaypointId != null) {
            modified |= WaypointData.updateCompassInTag(tag, unlinkWaypointId);
        }
        for (String key : tag.getAllKeys()) {
            Tag child = tag.get(key);
            if (child instanceof CompoundTag childCompound) {
                if ((childCompound.contains("id", 8) || childCompound.contains("Item", 8))
                        && (childCompound.contains("Count") || childCompound.contains("count"))) {
                    ItemStack inner = ItemStack.of(childCompound);
                    if (!inner.isEmpty() && stackUpdater.test(inner)) {
                        CompoundTag saved = inner.save(new CompoundTag());
                        if (childCompound.contains("Slot")) {
                            saved.put("Slot", childCompound.get("Slot").copy());
                        }
                        tag.put(key, saved);
                        modified = true;
                        continue;
                    }
                }
                modified |= updateLegacyNbtTree(childCompound, stackUpdater, unlinkWaypointId, depth + 1);
            } else if (child instanceof ListTag childList) {
                for (int i = 0; i < childList.size(); i++) {
                    Tag el = childList.get(i);
                    if (el instanceof CompoundTag compoundEl) {
                        if ((compoundEl.contains("id", 8) || compoundEl.contains("Item", 8))
                                && (compoundEl.contains("Count") || compoundEl.contains("count"))) {
                            ItemStack inner = ItemStack.of(compoundEl);
                            if (!inner.isEmpty() && stackUpdater.test(inner)) {
                                CompoundTag saved = inner.save(new CompoundTag());
                                if (compoundEl.contains("Slot")) {
                                    saved.put("Slot", compoundEl.get("Slot").copy());
                                }
                                childList.set(i, saved);
                                modified = true;
                                continue;
                            }
                        }
                        modified |= updateLegacyNbtTree(compoundEl, stackUpdater, unlinkWaypointId, depth + 1);
                    }
                }
            }
        }
        return modified;
    }
    *///?}

    private static boolean updateBundle(ItemStack bundleStack, Predicate<ItemStack> update) {
        //? if >=1.20.5 {
        BundleContents bundleContents = bundleStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return false;
        }

        List<ItemStack> updatedItems = new ArrayList<>();
        boolean modified = false;
        //? if >=26.3 {
        Iterator<ItemStack> items = bundleContents.itemCopies().iterator();
        //?} else {
        /*Iterator<ItemStack> items = bundleContents.itemCopyStream().iterator();
        *///?}
        while (items.hasNext()) {
            ItemStack copy = items.next();
            modified |= update.test(copy);
            updatedItems.add(copy);
        }

        if (modified) {
            //? if >=26.1 {
            bundleStack.set(DataComponents.BUNDLE_CONTENTS, new net.minecraft.world.item.component.BundleContents(
                    updatedItems.stream().map(st -> new net.minecraft.world.item.ItemStackTemplate(st.getItem().builtInRegistryHolder(), st.getCount(), st.getComponentsPatch())).toList()
            ));
            //?} else {
            /*bundleStack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(updatedItems));
            *///?}
        }
        return modified;
        //?} else {
        /*return false;
        *///?}
    }
}