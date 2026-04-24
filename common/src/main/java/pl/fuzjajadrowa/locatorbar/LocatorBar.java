package pl.fuzjajadrowa.locatorbar;

import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;

public final class LocatorBar {
    public static final String MOD_ID = "locatorbar";

    private LocatorBar() {
    }

    public static void init() {
        LocatorBarConfig.load();
    }
}