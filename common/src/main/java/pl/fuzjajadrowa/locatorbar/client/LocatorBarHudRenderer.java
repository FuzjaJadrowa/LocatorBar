package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

public final class LocatorBarHudRenderer {
    private static final ResourceLocation LOCATOR_BAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/locator_bar_background.png"
    );
    private static final int WIDTH = 51;
    private static final int HEIGHT = 5;
    private static final int TOP_MARGIN = 8;

    private LocatorBarHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }

        int x = (minecraft.getWindow().getGuiScaledWidth() - WIDTH) / 2;
        int y = TOP_MARGIN;
        guiGraphics.blit(LOCATOR_BAR_BACKGROUND, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }
}