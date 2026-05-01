package pl.fuzjajadrowa.locatorbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.CoordinatesFormat;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.DaysDisplayOrder;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ReworkedLocatorBarHudRenderer {
    private static final ResourceLocation LOCATOR_BAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/reworked_locator_bar_background.png"
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
    private static final ResourceLocation WAYPOINT = ResourceLocation.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/waypoint.png"
    );
    private static final int BAR_TEXTURE_WIDTH = 102;
    private static final int BAR_TEXTURE_HEIGHT = 10;
    private static final int BAR_MARGIN = 5;
    private static final int ICON_TEXTURE_SIZE = 36;
    private static final int ICON_MARGIN = 4;
    private static final int ICON_DOT_SIZE = ICON_TEXTURE_SIZE - (ICON_MARGIN * 2);
    private static final int BASE_DIRECTION_MARKER_SIZE = 12;
    private static final int BASE_DIRECTION_OVERFLOW = 2;
    private static final int BASE_PLAYER_HEAD_MARKER_SIZE = 12;
    private static final int BASE_PLAYER_HEAD_OVERFLOW = 2;
    private static final int PLAYER_HEAD_TEXTURE_SIZE = 64;
    private static final float PLAYER_FADE_START_DISTANCE = 45.0F;
    private static final float PLAYER_FADE_TO_MIN_DISTANCE = 260.0F;
    private static final float PLAYER_HIDE_DISTANCE = 330.0F;
    private static final float PLAYER_MIN_ALPHA = 0.010F;
    private static final int WAYPOINT_TEXTURE_SIZE = 36;
    private static final int BASE_WAYPOINT_MARKER_SIZE = 14;
    private static final float WAYPOINT_TEXT_SCALE = 0.75F;

    private ReworkedLocatorBarHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        if (!LocatorBarConfig.isEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }

        float scale = LocatorBarConfig.getScale();
        float halfViewAngle = LocatorBarConfig.getViewAngle() / 2.0F;
        int scaledBarWidth = Math.max(1, Math.round(BAR_TEXTURE_WIDTH * scale));
        int scaledBarHeight = Math.max(1, Math.round(BAR_TEXTURE_HEIGHT * scale));
        int directionMarkerSize = Math.max(4, Math.round(BASE_DIRECTION_MARKER_SIZE * LocatorBarConfig.getWorldDirectionsScale()));
        int playerHeadMarkerSize = Math.max(6, Math.round(BASE_PLAYER_HEAD_MARKER_SIZE * LocatorBarConfig.getPlayerHeadsScale()));
        int waypointMarkerSize = Math.max(6, Math.round(BASE_WAYPOINT_MARKER_SIZE * LocatorBarConfig.getWaypointsScale()));
        int waypointTopOverflow = Math.round(waypointMarkerSize * (8.0F / WAYPOINT_TEXTURE_SIZE));
        int waypointBottomOverflow = Math.round(waypointMarkerSize * (4.0F / WAYPOINT_TEXTURE_SIZE));
        int directionOverflow = Math.max(BASE_DIRECTION_OVERFLOW, ((directionMarkerSize - BAR_TEXTURE_HEIGHT) / 2) + BASE_DIRECTION_OVERFLOW);
        int playerHeadOverflow = Math.max(BASE_PLAYER_HEAD_OVERFLOW, ((playerHeadMarkerSize - BAR_TEXTURE_HEIGHT) / 2) + BASE_PLAYER_HEAD_OVERFLOW);

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int x = switch (LocatorBarConfig.getOffset()) {
            case LEFT -> BAR_MARGIN;
            case RIGHT -> screenWidth - scaledBarWidth - BAR_MARGIN;
            case CENTER -> (screenWidth - scaledBarWidth) / 2;
        };
        x = Math.max(0, x);
        int y = 5;

        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        float yaw = wrapTo180(player.getYRot());
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
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);

        guiGraphics.blit(RenderType::guiTextured, LOCATOR_BAR_BACKGROUND, 0, 0, 0, 0, BAR_TEXTURE_WIDTH, BAR_TEXTURE_HEIGHT, BAR_TEXTURE_WIDTH, BAR_TEXTURE_HEIGHT);
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
            for (WaypointMarker marker : collectWaypointMarkers(player)) {
                String displayText = marker.symbol();
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
                        waypointMarkerSize
                )) {
                    renderedWaypoints++;
                    if (renderedWaypoints >= maxWaypoints) {
                        break;
                    }
                }
            }
        }

        if (LocatorBarConfig.isShowPlayerHeads()) {
            List<PlayerHeadMarker> markers = collectPlayerHeadMarkers(player);
            int maxVisible = Math.min(markers.size(), LocatorBarConfig.getMaxVisiblePlayers());
            for (int i = 0; i < maxVisible; i++) {
                renderPlayerHeadMarker(
                        guiGraphics,
                        markers.get(i),
                        yaw,
                        halfViewAngle,
                        centerX,
                        headMarkerY,
                        playerHeadMarkerSize,
                        LocatorBarConfig.isPlayerHeadOutline()
                );
            }
        }

        guiGraphics.pose().popPose();
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

    private static void renderDirectionMarker(
            GuiGraphics guiGraphics,
            ResourceLocation texture,
            float directionYaw,
            float playerYaw,
            float halfViewAngle,
            float centerX,
            int markerY,
            int directionMarkerSize
    ) {
        float relative = wrapTo180(directionYaw - playerYaw);
        if (Math.abs(relative) > halfViewAngle) {
            return;
        }

        float normalized = relative / halfViewAngle;
        float markerX = quantizeToHalfPixel(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (directionMarkerSize / 2.0F));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(markerX, markerY, 0.0F);
        guiGraphics.blit(
                RenderType::guiTextured,
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
        guiGraphics.pose().popPose();
    }

    private static boolean renderWaypointMarker(
            GuiGraphics guiGraphics,
            WaypointMarker marker,
            String displayText,
            float playerYaw,
            float halfViewAngle,
            float centerX,
            int markerY,
            int waypointMarkerSize
    ) {
        float relative = wrapTo180(marker.directionYaw() - playerYaw);
        if (Math.abs(relative) > halfViewAngle) {
            return false;
        }

        float normalized = relative / halfViewAngle;
        float markerX = centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (waypointMarkerSize / 2.0F);
        float red = ((marker.rgbColor() >> 16) & 0xFF) / 255.0F;
        float green = ((marker.rgbColor() >> 8) & 0xFF) / 255.0F;
        float blue = (marker.rgbColor() & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(markerX, markerY, 0.0F);
        guiGraphics.blit(
                RenderType::guiTextured,
                WAYPOINT,
                0,
                0,
                0,
                0,
                waypointMarkerSize,
                waypointMarkerSize,
                WAYPOINT_TEXTURE_SIZE,
                WAYPOINT_TEXTURE_SIZE,
                WAYPOINT_TEXTURE_SIZE,
                WAYPOINT_TEXTURE_SIZE
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float dynamicTextScale = WAYPOINT_TEXT_SCALE * (waypointMarkerSize / (float) BASE_WAYPOINT_MARKER_SIZE);
        float textWidth = Minecraft.getInstance().font.width(displayText) * dynamicTextScale;
        float textHeight = Minecraft.getInstance().font.lineHeight * dynamicTextScale;
        float textX = ((waypointMarkerSize - textWidth) / 2.0F) + 0.45F;
        float textY = (waypointMarkerSize - textHeight) / 2.0F;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(textX, textY, 0.1F);
        guiGraphics.pose().scale(dynamicTextScale, dynamicTextScale, 1.0F);
        guiGraphics.drawString(Minecraft.getInstance().font, displayText, 0, 0, 0xFFFFFF, false);
        guiGraphics.pose().popPose();
        guiGraphics.pose().popPose();
        RenderSystem.disableBlend();
        return true;
    }


    private static void renderPlayerHeadMarker(
            GuiGraphics guiGraphics,
            PlayerHeadMarker marker,
            float playerYaw,
            float halfViewAngle,
            float centerX,
            int markerY,
            int markerSize,
            boolean outline
    ) {
        float relative = wrapTo180(marker.directionYaw() - playerYaw);
        if (Math.abs(relative) > halfViewAngle) {
            return;
        }

        float normalized = relative / halfViewAngle;
        float markerX = quantizeToHalfPixel(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (markerSize / 2.0F));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(markerX, markerY, 0.0F);

        int drawOffset = 0;
        int drawSize = markerSize;
        if (outline) {
            int alpha = Math.max(0, Math.min(255, Math.round(marker.alpha() * 255.0F)));
            guiGraphics.fill(0, 0, markerSize, markerSize, alpha << 24);
            int border = Math.max(1, Math.round(markerSize * 0.14F));
            drawOffset = border;
            drawSize = Math.max(1, markerSize - (border * 2));
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, marker.alpha());
        blitPlayerHead(guiGraphics, marker.skinTexture(), drawOffset, drawOffset, drawSize);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    private static void blitPlayerHead(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int size) {
        guiGraphics.blit(
                RenderType::guiTextured,
                texture,
                x,
                y,
                8,
                8,
                size,
                size,
                8,
                8,
                PLAYER_HEAD_TEXTURE_SIZE,
                PLAYER_HEAD_TEXTURE_SIZE
        );
        guiGraphics.blit(
                RenderType::guiTextured,
                texture,
                x,
                y,
                40,
                8,
                size,
                size,
                8,
                8,
                PLAYER_HEAD_TEXTURE_SIZE,
                PLAYER_HEAD_TEXTURE_SIZE
        );
    }

    private static void renderInfoText(GuiGraphics guiGraphics, Player player, float centerX, int startY, float scale) {
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
            long days = player.level().getDayTime() / 24000L;
            daysText = "Day " + days;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);
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

        guiGraphics.pose().popPose();
    }

    private static void drawCenteredText(GuiGraphics guiGraphics, String text, float centerX, int y) {
        int textX = Math.round(centerX - (Minecraft.getInstance().font.width(text) / 2.0F));
        guiGraphics.drawString(Minecraft.getInstance().font, text, textX, y, 0xFFFFFF, false);
    }

    private static List<WaypointMarker> collectWaypointMarkers(Player localPlayer) {
        List<WaypointMarker> markers = new ArrayList<>();
        UUID localPlayerId = localPlayer.getUUID();

        for (ItemStack stack : localPlayer.getInventory().items) {
            addWaypointMarker(markers, stack, localPlayer, localPlayerId);
        }
        for (ItemStack stack : localPlayer.getInventory().offhand) {
            addWaypointMarker(markers, stack, localPlayer, localPlayerId);
        }

        markers.sort(
                Comparator.comparingInt((WaypointMarker marker) -> marker.index() > 0 ? marker.index() : Integer.MAX_VALUE)
                        .thenComparing(WaypointMarker::waypointId)
        );
        return markers;
    }

    private static void addWaypointMarker(List<WaypointMarker> markers, ItemStack stack, Player localPlayer, UUID localPlayerId) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) {
            return;
        }

        UUID owner = WaypointData.getOwner(stack);
        if (owner != null && !owner.equals(localPlayerId)) {
            return;
        }

        GlobalPos target = tracker.target().get();
        if (!target.dimension().equals(localPlayer.level().dimension())) {
            return;
        }

        double dx = target.pos().getX() + 0.5D - localPlayer.getX();
        double dz = target.pos().getZ() + 0.5D - localPlayer.getZ();
        if (dx * dx + dz * dz < 1.0E-6D) {
            return;
        }

        UUID waypointId = WaypointData.getWaypointId(stack);
        if (waypointId == null) {
            String fallbackSeed = target.dimension().location() + "|" + target.pos().toShortString();
            waypointId = UUID.nameUUIDFromBytes(fallbackSeed.getBytes(StandardCharsets.UTF_8));
        }

        int index = WaypointData.getWaypointIndex(stack);
        if (WaypointData.isHidden(stack)) {
            return;
        }
        Integer customColor = WaypointData.getCustomColor(stack);
        int color = customColor == null ? colorFromWaypointId(waypointId) : customColor;
        String symbol = WaypointData.getWaypointSymbol(stack);
        float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        markers.add(new WaypointMarker(waypointId, wrapTo180(directionYaw), color, index, symbol));
    }

    private static int colorFromWaypointId(UUID waypointId) {
        long hash = waypointId.getMostSignificantBits() ^ waypointId.getLeastSignificantBits();
        float hue = (hash & 0xFFFFL) / 65535.0F;
        float saturation = 0.65F + (((hash >>> 16) & 0xFFL) / 255.0F) * 0.25F;
        float value = 0.8F + (((hash >>> 24) & 0xFFL) / 255.0F) * 0.2F;
        return Mth.hsvToRgb(hue, saturation, value);
    }

    private static List<PlayerHeadMarker> collectPlayerHeadMarkers(Player localPlayer) {
        List<PlayerHeadMarker> markers = new ArrayList<>();
        for (Player otherPlayer : localPlayer.level().players()) {
            if (otherPlayer == localPlayer) {
                continue;
            }

            if (shouldHidePlayerHead(localPlayer, otherPlayer)) {
                continue;
            }

            float alpha = computePlayerAlpha(localPlayer, otherPlayer);
            if (alpha <= 0.0F) {
                continue;
            }

            double dx = otherPlayer.getX() - localPlayer.getX();
            double dz = otherPlayer.getZ() - localPlayer.getZ();
            if (dx * dx + dz * dz < 1.0E-6D) {
                continue;
            }

            float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float distance = (float) Math.sqrt(dx * dx + dz * dz);
            PlayerSkin playerSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(otherPlayer.getGameProfile());
            markers.add(new PlayerHeadMarker(playerSkin.texture(), wrapTo180(directionYaw), alpha, distance));
        }
        markers.sort(Comparator.comparingDouble(PlayerHeadMarker::distance));
        return markers;
    }

    private static boolean shouldHidePlayerHead(Player localPlayer, Player otherPlayer) {
        if (!otherPlayer.level().dimension().equals(localPlayer.level().dimension())) {
            return true;
        }
        if (otherPlayer.isCrouching()) {
            return true;
        }

        ItemStack helmet = otherPlayer.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) {
            return false;
        }

        Item helmetItem = helmet.getItem();
        return helmetItem == Items.CARVED_PUMPKIN
                || helmetItem == Items.SKELETON_SKULL
                || helmetItem == Items.WITHER_SKELETON_SKULL
                || helmetItem == Items.ZOMBIE_HEAD
                || helmetItem == Items.CREEPER_HEAD
                || helmetItem == Items.DRAGON_HEAD
                || helmetItem == Items.PIGLIN_HEAD;
    }

    private static float computePlayerAlpha(Player localPlayer, Player otherPlayer) {
        float distance = horizontalDistance(localPlayer, otherPlayer);
        if (distance <= PLAYER_FADE_START_DISTANCE) {
            return 1.0F;
        }
        if (distance <= PLAYER_FADE_TO_MIN_DISTANCE) {
            float progress = (distance - PLAYER_FADE_START_DISTANCE) / (PLAYER_FADE_TO_MIN_DISTANCE - PLAYER_FADE_START_DISTANCE);
            float curvedProgress = (float) Math.pow(progress, 1.65D);
            return 1.0F - (curvedProgress * (1.0F - PLAYER_MIN_ALPHA));
        }
        if (distance < PLAYER_HIDE_DISTANCE) {
            return PLAYER_MIN_ALPHA;
        }
        return 0.0F;
    }

    private static float horizontalDistance(Player localPlayer, Player otherPlayer) {
        double dx = otherPlayer.getX() - localPlayer.getX();
        double dz = otherPlayer.getZ() - localPlayer.getZ();
        return (float) Math.sqrt(dx * dx + dz * dz);
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

    private static float quantizeToHalfPixel(float value) {
        return Math.round(value * 2.0F) / 2.0F;
    }

    private record WaypointMarker(UUID waypointId, float directionYaw, int rgbColor, int index, String symbol) {
    }

    private record PlayerHeadMarker(ResourceLocation skinTexture, float directionYaw, float alpha, float distance) {
    }
}