package pl.fuzjajadrowa.locatorbar;

import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;

import java.util.UUID;
import java.util.function.Consumer;

public final class LocatorBar {
    public static final String MOD_ID = "locatorbar";

    public interface NetworkBroadcaster {
        void broadcastConfig(pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings settings);
    }

    private static NetworkBroadcaster broadcaster = null;
    private static Consumer<UUID> unlinkWaypointSender = null;

    private LocatorBar() {
    }

    public static void setBroadcaster(NetworkBroadcaster networkBroadcaster) {
        broadcaster = networkBroadcaster;
    }

    public static void broadcastServerConfig(LocatorBarServerConfig.ServerSettings settings) {
        NetworkBroadcaster current = broadcaster;
        if (current != null) {
            current.broadcastConfig(settings);
        }
    }

    public static void setUnlinkWaypointSender(Consumer<UUID> sender) {
        unlinkWaypointSender = sender;
    }

    public static void sendUnlinkWaypoint(UUID waypointId) {
        Consumer<UUID> sender = unlinkWaypointSender;
        if (sender != null && waypointId != null) {
            sender.accept(waypointId);
        }
    }

    public static void init(boolean loadClientConfig) {
        if (loadClientConfig) {
            LocatorBarConfig.load();
        } else {
            LocatorBarServerConfig.load();
        }
    }
}