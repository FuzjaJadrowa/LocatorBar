package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;

public final class LocatorBarHudRenderer {
    private LocatorBarHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics) {
        // May be used in future
        /*
        //? if >=1.21.4 {
        //? if <1.21.11 {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player != null && !LocatorBarConfig.isUnsupportedVersionWarningShown()) {
            minecraft.getToastManager().addToast(new SupportWarningToast());
            LocatorBarConfig.setUnsupportedVersionWarningShown(true);
            LocatorBarConfig.save();
        }
        //?}
        //?}
        */

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