package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;
import pl.fuzjajadrowa.locatorbar.util.LocatorBarUtils;

import java.util.List;

final class MarkerRenderer {
    private MarkerRenderer() {}
    private static final Identifier NORTH = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/north.png"
    );
    private static final Identifier SOUTH = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/south.png"
    );
    private static final Identifier EAST = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/east.png"
    );
    private static final Identifier WEST = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/west.png"
    );
    private static final Identifier WAYPOINT = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/waypoint.png"
    );
    private static final Identifier DEATH_WAYPOINT = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/death_waypoint.png"
    );
    private static final int ICON_TEXTURE_SIZE = 36;
    private static final int ICON_MARGIN = 4;
    private static final int ICON_DOT_SIZE = 28;
    private static final int WAYPOINT_TEXTURE_SIZE = 36;
    private static final Identifier DOT_TEXTURE = Identifier.fromNamespaceAndPath(LocatorBar.MOD_ID, "textures/gui/locator_bar_dot.png");

    record Layout(int barHeight, int baseWaypointSize, float waypointTextScale, int deathOffset) {}

    static void render(GuiGraphicsExtractor guiGraphics, Player player, float yaw, float halfViewAngle,
                       float centerX, int directionMarkerY, int directionMarkerSize, int waypointMarkerY,
                       int waypointMarkerSize, int headMarkerY, int playerHeadMarkerSize, Layout layout) {
        if (LocatorBarConfig.isShowWorldDirections()) {
            renderDirectionMarker(guiGraphics, NORTH, 180.0F, yaw, halfViewAngle, centerX, directionMarkerY, directionMarkerSize);
            renderDirectionMarker(guiGraphics, SOUTH, 0.0F, yaw, halfViewAngle, centerX, directionMarkerY, directionMarkerSize);
            renderDirectionMarker(guiGraphics, EAST, -90.0F, yaw, halfViewAngle, centerX, directionMarkerY, directionMarkerSize);
            renderDirectionMarker(guiGraphics, WEST, 90.0F, yaw, halfViewAngle, centerX, directionMarkerY, directionMarkerSize);
        }

        if (LocatorBarConfig.isShowWaypoints()) {
            int fallbackIndex = 1;
            int renderedWaypoints = 0;
            int maxWaypoints = LocatorBarConfig.getMaxVisibleWaypoints();
            for (LocatorBarHudHelper.WaypointMarker marker : LocatorBarHudHelper.collectWaypointMarkers(player)) {
                String displayText = marker.symbol();
                boolean defaultIndexText = displayText == null || displayText.isEmpty();
                if (displayText == null || displayText.isEmpty()) {
                    int displayNumber = marker.index() > 0 ? marker.index() : fallbackIndex++;
                    displayText = Integer.toString(displayNumber);
                }
                if (renderWaypointMarker(
                        guiGraphics,
                        marker,
                        displayText,
                        yaw,
                        halfViewAngle,
                        centerX,
                        waypointMarkerY,
                        waypointMarkerSize,
                        defaultIndexText, layout
                )) {
                    renderedWaypoints++;
                    if (renderedWaypoints >= maxWaypoints) {
                        break;
                    }
                }
            }
        }

        PlayerMarkerType markerType = LocatorBarConfig.getPlayerMarkerType();
        boolean outline = LocatorBarConfig.isPlayerMarkerOutline();
        if (markerType != PlayerMarkerType.OFF) {
            List<PlayerLocatorClient.Marker> markers = LocatorBarHudHelper.collectPlayerMarkers(player);
            int maxVisible = Math.min(markers.size(), LocatorBarConfig.getMaxVisiblePlayers());
            for (int i = 0; i < maxVisible; i++) {
                renderPlayerMarker(
                        guiGraphics,
                        markers.get(i),
                        yaw,
                        halfViewAngle,
                        centerX,
                        headMarkerY,
                        playerHeadMarkerSize,
                        outline, markerType
                );
            }
        }

    }

    private static void renderDirectionMarker(
            GuiGraphicsExtractor guiGraphics,
            Identifier texture,
            float directionYaw,
            float playerYaw,
            float halfViewAngle,
            float centerX,
            int markerY,
            int directionMarkerSize
    ) {
        float relative = LocatorBarUtils.wrapTo180(directionYaw - playerYaw);
        if (Math.abs(relative) > halfViewAngle) {
            return;
        }

        float normalized = relative / halfViewAngle;
        float markerX = LocatorBarUtils.quantizeToHalfPixel(centerX + normalized * centerX - (directionMarkerSize / 2.0F));

        RenderCompat.push(guiGraphics);
        RenderCompat.translate(guiGraphics, markerX, markerY);
        RenderCompat.blitRegion(
                guiGraphics,
                texture,
                0,
                0,
                ICON_MARGIN,
                ICON_MARGIN,
                directionMarkerSize,
                directionMarkerSize,
                ICON_DOT_SIZE,
                ICON_DOT_SIZE,
                ICON_TEXTURE_SIZE,
                ICON_TEXTURE_SIZE
        );
        RenderCompat.pop(guiGraphics);
    }

    private static boolean renderWaypointMarker(
            GuiGraphicsExtractor guiGraphics,
            LocatorBarHudHelper.WaypointMarker marker,
            String displayText,
            float playerYaw,
            float halfViewAngle,
            float centerX,
            int markerY,
            int waypointMarkerSize,
            boolean defaultIndexText, Layout layout
    ) {
        float relative = LocatorBarUtils.wrapTo180(marker.directionYaw() - playerYaw);
        if (Math.abs(relative) > halfViewAngle) {
            return false;
        }

        float normalized = relative / halfViewAngle;
        float markerX = centerX + normalized * centerX - (waypointMarkerSize / 2.0F);
        int drawY = marker.isDeath() ? (layout.barHeight() - waypointMarkerSize) / 2 + layout.deathOffset() : markerY;
        RenderCompat.push(guiGraphics);
        RenderCompat.translate(guiGraphics, markerX, drawY);
        Identifier texture = marker.isDeath() ? DEATH_WAYPOINT : WAYPOINT;
        RenderCompat.blitTinted(
                guiGraphics,
                texture,
                0,
                0,
                0,
                0,
                waypointMarkerSize,
                waypointMarkerSize,
                WAYPOINT_TEXTURE_SIZE,
                WAYPOINT_TEXTURE_SIZE,
                WAYPOINT_TEXTURE_SIZE,
                WAYPOINT_TEXTURE_SIZE,
                0xFF000000 | marker.rgbColor()
        );

        if (marker.isDeath()) {
            RenderCompat.pop(guiGraphics);
            return true;
        }

        float dynamicTextScale = layout.waypointTextScale() * (waypointMarkerSize / (float) layout.baseWaypointSize());
        if (defaultIndexText && displayText.length() > 1) {
            dynamicTextScale *= 0.54F;
        }
        float textWidth = Minecraft.getInstance().font.width(displayText) * dynamicTextScale;
        float textHeight = Minecraft.getInstance().font.lineHeight * dynamicTextScale;
        float textX = ((waypointMarkerSize - textWidth) / 2.0F) + 0.45F;
        float textY = (waypointMarkerSize - textHeight) / 2.0F;
        RenderCompat.push(guiGraphics);
        RenderCompat.translate(guiGraphics, textX, textY);
        RenderCompat.scale(guiGraphics, dynamicTextScale, dynamicTextScale);
        RenderCompat.text(guiGraphics, displayText, 0, 0, 0xFFFFFFFF, false);
        RenderCompat.pop(guiGraphics);
        RenderCompat.pop(guiGraphics);
        return true;
    }

    private static void renderPlayerMarker(
            GuiGraphicsExtractor guiGraphics,
            PlayerLocatorClient.Marker marker,
            float playerYaw,
            float halfViewAngle,
            float centerX,
            int markerY,
            int markerSize,
            boolean outline, PlayerMarkerType markerType
    ) {
        float relative = LocatorBarUtils.wrapTo180(marker.directionYaw() - playerYaw);
        if (Math.abs(relative) > halfViewAngle) {
            return;
        }

        float normalized = relative / halfViewAngle;
        float markerX = LocatorBarUtils.quantizeToHalfPixel(centerX + normalized * centerX - (markerSize / 2.0F));

        RenderCompat.push(guiGraphics);
        RenderCompat.translate(guiGraphics, markerX, markerY);

        int alpha = Math.max(0, Math.min(255, Math.round(marker.alpha() * 255.0F)));

        if (markerType == PlayerMarkerType.DOTS) {
            Identifier dotTexture = DOT_TEXTURE;
            int playerColor = marker.teamColor() != null ? marker.teamColor() : LocatorBarUtils.colorFromPlayerId(marker.playerId());
            int tint = (alpha << 24) | (playerColor & 0x00FFFFFF);
            int dotSize = Math.round(markerSize * 1.5F);
            float offset = (markerSize - dotSize) / 2.0F;
            RenderCompat.push(guiGraphics);
            RenderCompat.translate(guiGraphics, offset, offset);
            RenderCompat.blitTinted(guiGraphics, dotTexture, 0, 0, 0, 0, dotSize, dotSize, 8, 8, 8, 8, tint);
            RenderCompat.pop(guiGraphics);
        } else {
            int drawOffset = 0;
            int drawSize = markerSize;
            if (outline) {
                guiGraphics.fill(0, 0, markerSize, markerSize, alpha << 24);
                int border = Math.max(1, Math.round(markerSize * 0.14F));
                drawOffset = border;
                drawSize = Math.max(1, markerSize - (border * 2));
            }
            int tint = (alpha << 24) | 0x00FFFFFF;
            RenderCompat.blitPlayerHead(guiGraphics, marker.skinTexture(), drawOffset, drawOffset, drawSize, tint);
        }
        RenderCompat.pop(guiGraphics);
    }
}