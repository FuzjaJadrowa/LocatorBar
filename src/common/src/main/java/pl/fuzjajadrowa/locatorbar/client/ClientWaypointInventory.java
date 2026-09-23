package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointInventory;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;

import java.util.ArrayList;
import java.util.List;

public final class ClientWaypointInventory {
    private static Player owner;
    private static Object level;
    private static List<ItemStack> items = List.of();
    private static boolean recoveryCompass;

    private ClientWaypointInventory() {}

    public static void tick(Player player) {
        owner = player;
        level = player == null ? null : player.level();
        recoveryCompass = false;
        if (player == null || !LocatorBarConfig.isEnabled() || !LocatorBarConfig.isShowWaypoints()) {
            items = List.of();
            return;
        }
        WaypointInventory.migrate(player);
        List<ItemStack> snapshot = new ArrayList<>();
        WaypointInventory.forEachLeaf(player, stack -> {
            if (stack.is(Items.RECOVERY_COMPASS)) recoveryCompass = true;
            if (WaypointData.getWaypointId(stack) != null) snapshot.add(stack.copy());
        });
        items = List.copyOf(snapshot);
    }

    static List<ItemStack> items(Player player) {
        return owner == player && level == player.level() ? items : List.of();
    }

    static boolean hasRecoveryCompass(Player player) {
        return owner == player && level == player.level() && recoveryCompass;
    }
}
