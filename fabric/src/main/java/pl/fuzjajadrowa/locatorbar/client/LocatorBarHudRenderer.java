package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.gui.GuiGraphics;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;

public final class LocatorBarHudRenderer {
    private LocatorBarHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        if (!LocatorBarConfig.isEnabled()) {
            return;
        }

        LocatorBarStyle style = LocatorBarConfig.getStyle();
        if (style == LocatorBarStyle.CLASSIC) {
            ClassicLocatorBarHudRenderer.render(guiGraphics);
            return;
        }

        ReworkedLocatorBarHudRenderer.render(guiGraphics);
    }
}