package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.CoordinatesFormat;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.DaysDisplayOrder;
import pl.fuzjajadrowa.locatorbar.util.LocatorBarUtils;

public final class ReworkedLocatorBarHudRenderer {
    private static final Identifier LOCATOR_BAR_BACKGROUND = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/reworked_locator_bar_background.png"
    );
    private static final int BAR_TEXTURE_WIDTH = 102;
    private static final int BAR_TEXTURE_HEIGHT = 10;
    private static final int BASE_DIRECTION_MARKER_SIZE = 12;
    private static final int BASE_DIRECTION_OVERFLOW = 2;
    private static final int BASE_PLAYER_HEAD_MARKER_SIZE = 12;
    private static final int BASE_PLAYER_HEAD_OVERFLOW = 2;
    private static final int WAYPOINT_TEXTURE_SIZE = 36;
    private static final int BASE_WAYPOINT_MARKER_SIZE = 14;

    private static final MarkerRenderer.Layout MARKER_LAYOUT = new MarkerRenderer.Layout(10, 14, 0.75F, 0);

    private ReworkedLocatorBarHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor guiGraphics) {
        if (!LocatorBarConfig.isEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        //? if >=26.2 {
        if (minecraft.gui.hud.isHidden()) {
            return;
        }
        //?} else {
        /*if (minecraft.options.hideGui) {
            return;
        }
        *///?}

        float scale = LocatorBarConfig.getScale();
        float halfViewAngle = LocatorBarConfig.getViewAngle() / 2.0F;
        int scaledBarWidth = Math.max(1, Math.round(BAR_TEXTURE_WIDTH * scale));
        int scaledBarHeight = Math.max(1, Math.round(BAR_TEXTURE_HEIGHT * scale));
        int directionMarkerSize = Math.max(4, Math.round(BASE_DIRECTION_MARKER_SIZE * LocatorBarConfig.getWorldDirectionsScale()));
        int playerHeadMarkerSize = Math.max(6, Math.round(BASE_PLAYER_HEAD_MARKER_SIZE * LocatorBarConfig.getPlayerMarkersScale()));
        int waypointMarkerSize = Math.max(6, Math.round(BASE_WAYPOINT_MARKER_SIZE * LocatorBarConfig.getWaypointsScale()));
        int waypointTopOverflow = Math.round(waypointMarkerSize * (8.0F / WAYPOINT_TEXTURE_SIZE));
        int waypointBottomOverflow = Math.round(waypointMarkerSize * (4.0F / WAYPOINT_TEXTURE_SIZE));
        int directionOverflow = Math.max(BASE_DIRECTION_OVERFLOW, ((directionMarkerSize - BAR_TEXTURE_HEIGHT) / 2) + BASE_DIRECTION_OVERFLOW);
        int playerHeadOverflow = Math.max(BASE_PLAYER_HEAD_OVERFLOW, ((playerHeadMarkerSize - BAR_TEXTURE_HEIGHT) / 2) + BASE_PLAYER_HEAD_OVERFLOW);

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int x = ((screenWidth - scaledBarWidth) / 2) + LocatorBarConfig.getCustomOffsetX();
        int y = 5 + LocatorBarConfig.getCustomOffsetY();

        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        float yaw = LocatorBarUtils.wrapTo180(player.getYRot());
        float centerX = BAR_TEXTURE_WIDTH / 2.0F;
        int directionMarkerY = -directionOverflow + ((BAR_TEXTURE_HEIGHT + (directionOverflow * 2) - directionMarkerSize) / 2);
        int headMarkerY = -playerHeadOverflow + ((BAR_TEXTURE_HEIGHT + (playerHeadOverflow * 2) - playerHeadMarkerSize) / 2);
        int waypointMarkerY = -waypointTopOverflow;
        int scissorOverflow = Math.max(
                Math.max(directionOverflow, playerHeadOverflow),
                Math.max(waypointTopOverflow, waypointBottomOverflow)
        );

        int scissorTop = y - Math.round(scissorOverflow * scale);
        int scissorBottom = y + Math.round((BAR_TEXTURE_HEIGHT + scissorOverflow) * scale);
        guiGraphics.enableScissor(x, scissorTop, x + scaledBarWidth, scissorBottom);
        RenderCompat.push(guiGraphics);
        RenderCompat.translate(guiGraphics, x, y);
        RenderCompat.scale(guiGraphics, scale, scale);

        RenderCompat.blit(guiGraphics, LOCATOR_BAR_BACKGROUND, 0, 0, 0, 0, BAR_TEXTURE_WIDTH, BAR_TEXTURE_HEIGHT, BAR_TEXTURE_WIDTH, BAR_TEXTURE_HEIGHT);
        MarkerRenderer.render(guiGraphics, player, yaw, halfViewAngle, centerX,
                directionMarkerY, directionMarkerSize, waypointMarkerY, waypointMarkerSize,
                headMarkerY, playerHeadMarkerSize, MARKER_LAYOUT);

        RenderCompat.pop(guiGraphics);
        guiGraphics.disableScissor();
        if (LocatorBarConfig.isShowCoordinates() || LocatorBarConfig.isShowDays()) {
            renderInfoText(
                    guiGraphics,
                    player,
                    x + (scaledBarWidth / 2.0F),
                    y + scaledBarHeight + Math.round(3.0F * scale),
                    scale
            );
        }
    }

    private static void renderInfoText(GuiGraphicsExtractor guiGraphics, Player player, float centerX, int startY, float scale) {
        String coordsText = null;
        if (LocatorBarConfig.isShowCoordinates()) {
            if (LocatorBarConfig.getCoordinatesFormat() == CoordinatesFormat.XZ) {
                coordsText = "(" + player.getBlockX() + " " + player.getBlockZ() + ")";
            } else {
                coordsText = "(" + player.getBlockX() + " " + player.getBlockY() + " " + player.getBlockZ() + ")";
            }
        }

        String daysText = null;
        if (LocatorBarConfig.isShowDays()) {
            //? if >=26.1
            long days = player.level().getOverworldClockTime() / 24000L;
            //? if <26.1
            /*long days = player.level().getDayTime() / 24000L;*/
            daysText = "Day " + days;
        }

        RenderCompat.push(guiGraphics);
        RenderCompat.scale(guiGraphics, scale, scale);
        float scaledCenterX = centerX / scale;
        int scaledStartY = Math.round(startY / scale);
        int lineStep = Minecraft.getInstance().font.lineHeight + 1;

        if (coordsText != null && daysText != null) {
            if (LocatorBarConfig.getDaysDisplayOrder() == DaysDisplayOrder.DAYS_UNDER_COORDS) {
                drawCenteredText(guiGraphics, coordsText, scaledCenterX, scaledStartY);
                drawCenteredText(guiGraphics, daysText, scaledCenterX, scaledStartY + lineStep);
            } else {
                drawCenteredText(guiGraphics, daysText, scaledCenterX, scaledStartY);
                drawCenteredText(guiGraphics, coordsText, scaledCenterX, scaledStartY + lineStep);
            }
        } else if (coordsText != null) {
            drawCenteredText(guiGraphics, coordsText, scaledCenterX, scaledStartY);
        } else if (daysText != null) {
            drawCenteredText(guiGraphics, daysText, scaledCenterX, scaledStartY);
        }

        RenderCompat.pop(guiGraphics);
    }

    private static void drawCenteredText(GuiGraphicsExtractor guiGraphics, String text, float centerX, int y) {
        int textX = Math.round(centerX - (Minecraft.getInstance().font.width(text) / 2.0F));
        RenderCompat.text(guiGraphics, text, textX, y, 0xFFFFFFFF, true);
    }
}