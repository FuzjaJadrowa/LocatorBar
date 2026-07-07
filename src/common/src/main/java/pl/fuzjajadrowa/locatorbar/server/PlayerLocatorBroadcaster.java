package pl.fuzjajadrowa.locatorbar.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings;
import pl.fuzjajadrowa.locatorbar.network.PlayerLocatorPayload;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class PlayerLocatorBroadcaster {
    public static final int UPDATE_INTERVAL_TICKS = 5;

    private PlayerLocatorBroadcaster() {
    }

    public static PlayerLocatorPayload createPayload(ServerPlayer viewer, List<ServerPlayer> players) {
        ServerSettings settings = LocatorBarServerConfig.get();
        if (settings == null) {
            settings = ServerSettings.defaults();
        }
        if (settings.playerMarkerType() == PlayerMarkerType.OFF) {
            return new PlayerLocatorPayload(List.of());
        }

        int maxVisiblePlayers = settings.maxVisiblePlayers();
        double maxDistance = settings.playerMarkerHideDistance();
        double maxDistanceSquared = maxDistance * maxDistance;
        PriorityQueue<PlayerEntry> closestEntries = new PriorityQueue<>(
                maxVisiblePlayers,
                Comparator.comparingDouble(PlayerEntry::distanceSquared).reversed()
        );

        for (ServerPlayer otherPlayer : players) {
            if (otherPlayer == viewer || shouldHidePlayerHead(viewer, otherPlayer)) {
                continue;
            }

            double dx = otherPlayer.getX() - viewer.getX();
            double dz = otherPlayer.getZ() - viewer.getZ();
            double distanceSquared = dx * dx + dz * dz;
            if (distanceSquared < 1.0E-6D || distanceSquared >= maxDistanceSquared) {
                continue;
            }

            PlayerEntry entry = new PlayerEntry(
                    new PlayerLocatorPayload.Entry(otherPlayer.getUUID(), otherPlayer.getX(), otherPlayer.getZ()),
                    distanceSquared
            );
            if (closestEntries.size() < maxVisiblePlayers) {
                closestEntries.add(entry);
                continue;
            }

            PlayerEntry farthestEntry = closestEntries.peek();
            if (farthestEntry != null && distanceSquared < farthestEntry.distanceSquared()) {
                closestEntries.poll();
                closestEntries.add(entry);
            }
        }

        List<PlayerEntry> entries = new ArrayList<>(closestEntries);
        entries.sort(Comparator.comparingDouble(PlayerEntry::distanceSquared));
        List<PlayerLocatorPayload.Entry> payloadEntries = new ArrayList<>(entries.size());
        for (PlayerEntry entry : entries) {
            payloadEntries.add(entry.entry());
        }
        return new PlayerLocatorPayload(List.copyOf(payloadEntries));
    }

    private static boolean shouldHidePlayerHead(ServerPlayer viewer, ServerPlayer otherPlayer) {
        if (!otherPlayer.level().dimension().equals(viewer.level().dimension())) {
            return true;
        }
        if (otherPlayer.isCrouching()) {
            return true;
        }

        ItemStack helmet = otherPlayer.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) {
            return false;
        }

        Item helmetItem = helmet.getItem();
        return helmetItem == Items.CARVED_PUMPKIN
                || helmetItem == Items.SKELETON_SKULL
                || helmetItem == Items.WITHER_SKELETON_SKULL
                || helmetItem == Items.ZOMBIE_HEAD
                || helmetItem == Items.CREEPER_HEAD
                || helmetItem == Items.DRAGON_HEAD
                || helmetItem == Items.PIGLIN_HEAD;
    }

    private record PlayerEntry(PlayerLocatorPayload.Entry entry, double distanceSquared) {
    }
}