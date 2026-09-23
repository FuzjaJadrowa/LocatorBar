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
    private static final PlayerLocatorPayload EMPTY_PAYLOAD = new PlayerLocatorPayload(List.of());

    private PlayerLocatorBroadcaster() {
    }

    public static PlayerLocatorPayload createPayload(ServerPlayer viewer, List<ServerPlayer> players) {
        ServerSettings settings = LocatorBarServerConfig.get();
        if (settings == null) {
            settings = ServerSettings.defaults();
        }
        if (settings.playerMarkerType() == PlayerMarkerType.OFF) {
            return EMPTY_PAYLOAD;
        }

        int maxVisiblePlayers = settings.maxVisiblePlayers();
        double maxDistance = settings.playerMarkerHideDistance();
        double maxDistanceSquared = maxDistance * maxDistance;
        PriorityQueue<PlayerEntry> closestEntries = new PriorityQueue<>(
                maxVisiblePlayers,
                Comparator.comparingDouble(PlayerEntry::distanceSquared).reversed()
        );
        double viewerX = viewer.getX();
        double viewerZ = viewer.getZ();

        for (ServerPlayer otherPlayer : players) {
            if (otherPlayer == viewer || LocatorBarUtils.shouldHidePlayerHead(viewer, otherPlayer)) {
                continue;
            }

            double dx = otherPlayer.getX() - viewerX;
            double dz = otherPlayer.getZ() - viewerZ;
            double distanceSquared = dx * dx + dz * dz;
            if (distanceSquared < 1.0E-6D || distanceSquared >= maxDistanceSquared) {
                continue;
            }

            if (closestEntries.size() == maxVisiblePlayers) {
                if (distanceSquared >= closestEntries.peek().distanceSquared()) continue;
                closestEntries.poll();
            }
            closestEntries.add(new PlayerEntry(
                    new PlayerLocatorPayload.Entry(otherPlayer.getUUID(), otherPlayer.getX(), otherPlayer.getZ()),
                    distanceSquared
            ));
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
