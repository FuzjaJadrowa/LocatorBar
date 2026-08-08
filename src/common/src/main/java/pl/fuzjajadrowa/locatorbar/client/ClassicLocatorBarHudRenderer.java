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
import java.util.UUID;

public final class ClassicLocatorBarHudRenderer {
    private static final Identifier CLASSIC_LOCATOR_BAR_BACKGROUND = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/classic_locator_bar_background.png"
    );
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
    private static final int BAR_TEXTURE_WIDTH = 182;
    private static final int BAR_TEXTURE_HEIGHT = 5;
    private static final int ICON_TEXTURE_SIZE = 36;
    private static final int ICON_MARGIN = 4;
    private static final int ICON_DOT_SIZE = ICON_TEXTURE_SIZE - (ICON_MARGIN * 2);
    private static final float CLASSIC_DIRECTIONS_DEFAULT_SCALE = 0.7F;
    private static final float CLASSIC_PLAYER_HEADS_DEFAULT_SCALE = 0.7F;
    private static final int BASE_DIRECTION_MARKER_SIZE = 12;
    private static final int BASE_DIRECTION_OVERFLOW = 2;
    private static final int BASE_PLAYER_HEAD_MARKER_SIZE = 12;
    private static final int BASE_PLAYER_HEAD_OVERFLOW = 2;
    private static final int WAYPOINT_TEXTURE_SIZE = 36;
    private static final int BASE_WAYPOINT_MARKER_SIZE = 10;
    private static final float WAYPOINT_TEXT_SCALE = 0.65F;
    private static final int WAYPOINT_Y_OFFSET = 2;

    private static final Identifier DOT_TEXTURE = Identifier.fromNamespaceAndPath(LocatorBar.MOD_ID, "textures/gui/locator_bar_dot.png");

    private ClassicLocatorBarHudRenderer() {
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

        //? if <1.21.11 {
        /*guiGraphics.flush();
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
        *///?}

        float halfViewAngle = 45.0F;
        int directionMarkerSize = Math.max(
                4,
                Math.round(BASE_DIRECTION_MARKER_SIZE * LocatorBarConfig.getWorldDirectionsScale() * CLASSIC_DIRECTIONS_DEFAULT_SCALE)
        );
        int playerHeadMarkerSize = Math.max(
                6,
                Math.round(BASE_PLAYER_HEAD_MARKER_SIZE * LocatorBarConfig.getPlayerMarkersScale() * CLASSIC_PLAYER_HEADS_DEFAULT_SCALE)
        );
        int waypointMarkerSize = Math.max(
                6,
                Math.round(BASE_WAYPOINT_MARKER_SIZE * LocatorBarConfig.getWaypointsScale())
        );
        int waypointTopOverflow = Math.round(waypointMarkerSize * (8.0F / WAYPOINT_TEXTURE_SIZE));
        int waypointBottomOverflow = Math.round(waypointMarkerSize * (4.0F / WAYPOINT_TEXTURE_SIZE));
        int directionOverflow = Math.max(BASE_DIRECTION_OVERFLOW, ((directionMarkerSize - BAR_TEXTURE_HEIGHT) / 2) + BASE_DIRECTION_OVERFLOW);
        int playerHeadOverflow = Math.max(BASE_PLAYER_HEAD_OVERFLOW, ((playerHeadMarkerSize - BAR_TEXTURE_HEIGHT) / 2) + BASE_PLAYER_HEAD_OVERFLOW);

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int x = ((screenWidth - BAR_TEXTURE_WIDTH) / 2) + LocatorBarConfig.getCustomOffsetX();
        int y = screenHeight - 29 + LocatorBarConfig.getCustomOffsetY();

        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        boolean vanillaExperienceBarVisible = isVanillaExperienceBarVisible(minecraft, player);
        boolean elementsOnXpBar = LocatorBarConfig.isElementsOnXpBar();
        if (!elementsOnXpBar && ClassicExperienceBarState.shouldShowVanillaExperienceBar(minecraft, player)) {
            return;
        }

        float yaw = LocatorBarUtils.wrapTo180(player.getYRot());
        float centerX = BAR_TEXTURE_WIDTH / 2.0F;
        int directionMarkerY = -directionOverflow + ((BAR_TEXTURE_HEIGHT + (directionOverflow * 2) - directionMarkerSize) / 2);
        int headMarkerY = -playerHeadOverflow + ((BAR_TEXTURE_HEIGHT + (playerHeadOverflow * 2) - playerHeadMarkerSize) / 2);
        int waypointMarkerY = -waypointTopOverflow - WAYPOINT_Y_OFFSET;
        int scissorOverflow = Math.max(
                Math.max(directionOverflow, playerHeadOverflow),
                Math.max(waypointTopOverflow + WAYPOINT_Y_OFFSET, waypointBottomOverflow)
        );

        int scissorTop = y - scissorOverflow;
        int scissorBottom = y + BAR_TEXTURE_HEIGHT + scissorOverflow;
        guiGraphics.enableScissor(x, scissorTop, x + BAR_TEXTURE_WIDTH, scissorBottom);
        RenderCompat.push(guiGraphics);
        RenderCompat.translate(guiGraphics, x, y);

        if (!vanillaExperienceBarVisible || !elementsOnXpBar) {
            RenderCompat.blit(
                    guiGraphics,
                    CLASSIC_LOCATOR_BAR_BACKGROUND,
                    0,
                    0,
                    0,
                    0,
                    BAR_TEXTURE_WIDTH,
                    BAR_TEXTURE_HEIGHT,
                    BAR_TEXTURE_WIDTH,
                    BAR_TEXTURE_HEIGHT
            );
        }

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
                        defaultIndexText
                )) {
                    renderedWaypoints++;
                    if (renderedWaypoints >= maxWaypoints) {
                        break;
                    }
                }
            }
        }

        if (LocatorBarConfig.getPlayerMarkerType() != PlayerMarkerType.OFF) {
            List<LocatorBarHudHelper.PlayerMarker> markers = LocatorBarHudHelper.collectPlayerMarkers(player);
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
                        LocatorBarConfig.isPlayerMarkerOutline()
                );
            }
        }

        RenderCompat.pop(guiGraphics);
        guiGraphics.disableScissor();
        //? if <1.21.11 {
        /*com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        guiGraphics.flush();
        *///?}
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
        float markerX = LocatorBarUtils.quantizeToHalfPixel(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (directionMarkerSize / 2.0F));

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
            boolean defaultIndexText
    ) {
        float relative = LocatorBarUtils.wrapTo180(marker.directionYaw() - playerYaw);
        if (Math.abs(relative) > halfViewAngle) {
            return false;
        }

        float normalized = relative / halfViewAngle;
        float markerX = centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (waypointMarkerSize / 2.0F);
        int drawY = marker.isDeath() ? (BAR_TEXTURE_HEIGHT - waypointMarkerSize) / 2 - 1 : markerY;
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

        float dynamicTextScale = WAYPOINT_TEXT_SCALE * (waypointMarkerSize / (float) BASE_WAYPOINT_MARKER_SIZE);
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
            LocatorBarHudHelper.PlayerMarker marker,
            float playerYaw,
            float halfViewAngle,
            float centerX,
            int markerY,
            int markerSize,
            boolean outline
    ) {
        float relative = LocatorBarUtils.wrapTo180(marker.directionYaw() - playerYaw);
        if (Math.abs(relative) > halfViewAngle) {
            return;
        }

        float normalized = relative / halfViewAngle;
        float markerX = LocatorBarUtils.quantizeToHalfPixel(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (markerSize / 2.0F));

        RenderCompat.push(guiGraphics);
        RenderCompat.translate(guiGraphics, markerX, markerY);

        int alpha = Math.max(0, Math.min(255, Math.round(marker.alpha() * 255.0F)));

        if (LocatorBarConfig.getPlayerMarkerType() == PlayerMarkerType.DOTS) {
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

    private static boolean isVanillaExperienceBarVisible(Minecraft minecraft, Player player) {
        return minecraft.gameMode != null && minecraft.gameMode.hasExperience() && !player.isSpectator();
    }
}