package pl.fuzjajadrowa.locatorbar.server;

import net.minecraft.server.level.ServerPlayer;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings;
import pl.fuzjajadrowa.locatorbar.network.PlayerLocatorPayload;
import pl.fuzjajadrowa.locatorbar.util.LocatorBarUtils;

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
            if (otherPlayer == viewer || LocatorBarUtils.shouldHidePlayerHead(viewer, otherPlayer)) {
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

    private record PlayerEntry(PlayerLocatorPayload.Entry entry, double distanceSquared) {
    }
}