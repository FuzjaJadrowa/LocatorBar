package pl.fuzjajadrowa.locatorbar;

import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;

public final class LocatorBar {
    public static final String MOD_ID = "locatorbar";

    public interface NetworkBroadcaster {
        void broadcastConfig(pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings settings);
    }

    public static NetworkBroadcaster broadcaster = null;
    private LocatorBar() {
    }

    public static void init(boolean loadClientConfig) {
        if (loadClientConfig) {
            LocatorBarConfig.load();
        } else {
            LocatorBarServerConfig.load();
        }
    }
}