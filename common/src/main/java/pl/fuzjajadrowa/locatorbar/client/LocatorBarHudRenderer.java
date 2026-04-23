package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

public final class LocatorBarHudRenderer {
    private static final ResourceLocation LOCATOR_BAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/locator_bar_background.png"
    );
    private static final ResourceLocation NORTH = ResourceLocation.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/north.png"
    );
    private static final ResourceLocation SOUTH = ResourceLocation.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/south.png"
    );
    private static final ResourceLocation EAST = ResourceLocation.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/east.png"
    );
    private static final ResourceLocation WEST = ResourceLocation.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/west.png"
    );
    private static final int WIDTH = 71;
    private static final int HEIGHT = 5;
    private static final int TOP_MARGIN = 5;
    private static final float VIEW_ANGLE = 90.0F;
    private static final float HALF_VIEW_ANGLE = VIEW_ANGLE / 2.0F;
    private static final int ICON_SIZE = 36;
    private static final int ICON_MARGIN = 4;
    private static final int ICON_DOT_SIZE = ICON_SIZE - (ICON_MARGIN * 2);
    private static final int MARKER_SIZE = 7;
    private static final int MARKER_OVERFLOW = 1;

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

        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        float yaw = wrapTo180(player.getYRot());
        float centerX = x + (WIDTH / 2.0F);
        int markerY = y - MARKER_OVERFLOW + ((HEIGHT + (MARKER_OVERFLOW * 2) - MARKER_SIZE) / 2);

        guiGraphics.enableScissor(x, y - MARKER_OVERFLOW, x + WIDTH, y + HEIGHT + MARKER_OVERFLOW);
        renderDirectionMarker(guiGraphics, NORTH, 180.0F, yaw, centerX, markerY);
        renderDirectionMarker(guiGraphics, SOUTH, 0.0F, yaw, centerX, markerY);
        renderDirectionMarker(guiGraphics, EAST, -90.0F, yaw, centerX, markerY);
        renderDirectionMarker(guiGraphics, WEST, 90.0F, yaw, centerX, markerY);
        guiGraphics.disableScissor();
    }

    private static void renderDirectionMarker(
            GuiGraphics guiGraphics,
            ResourceLocation texture,
            float directionYaw,
            float playerYaw,
            float centerX,
            int markerY
    ) {
        float relative = wrapTo180(directionYaw - playerYaw);
        if (Math.abs(relative) > HALF_VIEW_ANGLE) {
            return;
        }

        float normalized = relative / HALF_VIEW_ANGLE;
        int markerX = Math.round(centerX + normalized * (WIDTH / 2.0F) - (MARKER_SIZE / 2.0F));

        guiGraphics.blit(
                texture,
                markerX,
                markerY,
                MARKER_SIZE,
                MARKER_SIZE,
                ICON_MARGIN,
                ICON_MARGIN,
                ICON_DOT_SIZE,
                ICON_DOT_SIZE,
                ICON_SIZE,
                ICON_SIZE
        );
    }

    private static float wrapTo180(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        } else if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }
}