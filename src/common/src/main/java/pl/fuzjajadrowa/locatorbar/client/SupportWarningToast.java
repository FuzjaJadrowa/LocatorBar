package pl.fuzjajadrowa.locatorbar.client;

// May be used in future
/*
//? if >=1.21.4 {
//? if <1.21.11 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SupportWarningToast implements Toast {
    private static final Identifier BACKGROUND_SPRITE = Identifier.fromNamespaceAndPath("minecraft", "toast/advancement");
    private static final Identifier DEATH_WAYPOINT = Identifier.fromNamespaceAndPath("locatorbar", "textures/gui/death_waypoint.png");
    private Toast.Visibility visibility = Toast.Visibility.SHOW;

    public SupportWarningToast() {
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (time >= 10000L) {
            this.visibility = Toast.Visibility.HIDE;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, Font font, long startTime) {
        guiGraphics.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, BACKGROUND_SPRITE, 0, 0, 160, 32);
        RenderCompat.text(guiGraphics, "Locator Bar for 1.21.4", 30, 7, 0xFFFFFFFF, false);
        RenderCompat.text(guiGraphics, "won't be supported.", 30, 18, 0xFFFFFFFF, false);
        RenderCompat.blitRegion(guiGraphics, DEATH_WAYPOINT, 8, 8, 0, 0, 16, 16, 36, 36, 36, 36);
    }
}
//?}
//?}
*/