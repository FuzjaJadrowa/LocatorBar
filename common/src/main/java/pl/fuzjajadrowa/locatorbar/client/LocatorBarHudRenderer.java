package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;

public final class LocatorBarHudRenderer {
    private LocatorBarHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics) {
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