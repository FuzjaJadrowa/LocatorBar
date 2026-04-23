package pl.fuzjajadrowa.locatorbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ReworkedLocatorBarHudRenderer {
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
    private static final ResourceLocation WAYPOINT = ResourceLocation.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/waypoint.png"
    );
    private static final int BAR_TEXTURE_WIDTH = 102;
    private static final int BAR_TEXTURE_HEIGHT = 10;
    private static final int TOP_MARGIN = 5;
    private static final float VIEW_ANGLE = 90.0F;
    private static final float HALF_VIEW_ANGLE = VIEW_ANGLE / 2.0F;
    private static final int ICON_TEXTURE_SIZE = 36;
    private static final int ICON_MARGIN = 4;
    private static final int ICON_DOT_SIZE = ICON_TEXTURE_SIZE - (ICON_MARGIN * 2);
    private static final int DIRECTION_MARKER_SIZE = 12;
    private static final int DIRECTION_OVERFLOW = 2;
    private static final int PLAYER_HEAD_MARKER_SIZE = BAR_TEXTURE_HEIGHT + 8;
    private static final int PLAYER_HEAD_OVERFLOW = 2;
    private static final int PLAYER_HEAD_TEXTURE_SIZE = 64;
    private static final int PLAYER_HEAD_U = 8;
    private static final int PLAYER_HEAD_V = 8;
    private static final int PLAYER_HEAD_HAT_U = 40;
    private static final int PLAYER_HEAD_HAT_V = 8;
    private static final float PLAYER_FADE_START_DISTANCE = 100.0F;
    private static final float PLAYER_FADE_TO_MIN_DISTANCE = 400.0F;
    private static final float PLAYER_HIDE_DISTANCE = 450.0F;
    private static final float PLAYER_MIN_ALPHA = 0.2F;
    private static final int WAYPOINT_TEXTURE_SIZE = 36;
    private static final int WAYPOINT_MARKER_SIZE = 15;
    private static final int WAYPOINT_TOP_OVERFLOW = Math.round(WAYPOINT_MARKER_SIZE * (8.0F / WAYPOINT_TEXTURE_SIZE));
    private static final int WAYPOINT_BOTTOM_OVERFLOW = Math.round(WAYPOINT_MARKER_SIZE * (4.0F / WAYPOINT_TEXTURE_SIZE));
    private static final int WAYPOINT_TEXT_COLOR = 0x101010;

    private ReworkedLocatorBarHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }
        if (minecraft.options.keyPlayerList.isDown()) {
            return;
        }

        int x = (minecraft.getWindow().getGuiScaledWidth() - BAR_TEXTURE_WIDTH) / 2;
        int y = TOP_MARGIN;
        guiGraphics.blit(LOCATOR_BAR_BACKGROUND, x, y, 0, 0, BAR_TEXTURE_WIDTH, BAR_TEXTURE_HEIGHT, BAR_TEXTURE_WIDTH, BAR_TEXTURE_HEIGHT);

        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        float yaw = wrapTo180(player.getYRot());
        float centerX = x + (BAR_TEXTURE_WIDTH / 2.0F);
        int directionMarkerY = y - DIRECTION_OVERFLOW + ((BAR_TEXTURE_HEIGHT + (DIRECTION_OVERFLOW * 2) - DIRECTION_MARKER_SIZE) / 2);
        int headMarkerY = y - PLAYER_HEAD_OVERFLOW + ((BAR_TEXTURE_HEIGHT + (PLAYER_HEAD_OVERFLOW * 2) - PLAYER_HEAD_MARKER_SIZE) / 2);
        int waypointMarkerY = y - WAYPOINT_TOP_OVERFLOW;
        int scissorOverflow = Math.max(
                Math.max(DIRECTION_OVERFLOW, PLAYER_HEAD_OVERFLOW),
                Math.max(WAYPOINT_TOP_OVERFLOW, WAYPOINT_BOTTOM_OVERFLOW)
        );

        guiGraphics.enableScissor(x, y - scissorOverflow, x + BAR_TEXTURE_WIDTH, y + BAR_TEXTURE_HEIGHT + scissorOverflow);
        renderDirectionMarker(guiGraphics, NORTH, 180.0F, yaw, centerX, directionMarkerY);
        renderDirectionMarker(guiGraphics, SOUTH, 0.0F, yaw, centerX, directionMarkerY);
        renderDirectionMarker(guiGraphics, EAST, -90.0F, yaw, centerX, directionMarkerY);
        renderDirectionMarker(guiGraphics, WEST, 90.0F, yaw, centerX, directionMarkerY);

        int fallbackIndex = 1;
        for (WaypointMarker marker : collectWaypointMarkers(player)) {
            int displayNumber = marker.index() > 0 ? marker.index() : fallbackIndex++;
            renderWaypointMarker(guiGraphics, marker, displayNumber, yaw, centerX, waypointMarkerY);
        }

        for (PlayerHeadMarker marker : collectPlayerHeadMarkers(player)) {
            renderPlayerHeadMarker(guiGraphics, marker, yaw, centerX, headMarkerY);
        }

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
        float markerX = quantizeToHalfPixel(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (DIRECTION_MARKER_SIZE / 2.0F));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(markerX, markerY, 0.0F);
        guiGraphics.blit(
                texture,
                0,
                0,
                DIRECTION_MARKER_SIZE,
                DIRECTION_MARKER_SIZE,
                ICON_MARGIN,
                ICON_MARGIN,
                ICON_DOT_SIZE,
                ICON_DOT_SIZE,
                ICON_TEXTURE_SIZE,
                ICON_TEXTURE_SIZE
        );
        guiGraphics.pose().popPose();
    }

    private static void renderWaypointMarker(
            GuiGraphics guiGraphics,
            WaypointMarker marker,
            int displayNumber,
            float playerYaw,
            float centerX,
            int markerY
    ) {
        float relative = wrapTo180(marker.directionYaw() - playerYaw);
        if (Math.abs(relative) > HALF_VIEW_ANGLE) {
            return;
        }

        float normalized = relative / HALF_VIEW_ANGLE;
        float markerX = quantizeToHalfPixel(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (WAYPOINT_MARKER_SIZE / 2.0F));
        float red = ((marker.rgbColor() >> 16) & 0xFF) / 255.0F;
        float green = ((marker.rgbColor() >> 8) & 0xFF) / 255.0F;
        float blue = (marker.rgbColor() & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(markerX, markerY, 0.0F);
        guiGraphics.blit(
                WAYPOINT,
                0,
                0,
                WAYPOINT_MARKER_SIZE,
                WAYPOINT_MARKER_SIZE,
                0,
                0,
                WAYPOINT_TEXTURE_SIZE,
                WAYPOINT_TEXTURE_SIZE,
                WAYPOINT_TEXTURE_SIZE,
                WAYPOINT_TEXTURE_SIZE
        );
        guiGraphics.pose().popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        String text = Integer.toString(displayNumber);
        int textX = Math.round(markerX + (WAYPOINT_MARKER_SIZE - Minecraft.getInstance().font.width(text)) / 2.0F);
        int textY = markerY + ((WAYPOINT_MARKER_SIZE - Minecraft.getInstance().font.lineHeight) / 2);
        guiGraphics.drawString(Minecraft.getInstance().font, text, textX, textY, WAYPOINT_TEXT_COLOR, false);
        RenderSystem.disableBlend();
    }

    private static void renderPlayerHeadMarker(
            GuiGraphics guiGraphics,
            PlayerHeadMarker marker,
            float playerYaw,
            float centerX,
            int markerY
    ) {
        float relative = wrapTo180(marker.directionYaw() - playerYaw);
        if (Math.abs(relative) > HALF_VIEW_ANGLE) {
            return;
        }

        float normalized = relative / HALF_VIEW_ANGLE;
        float markerX = quantizeToHalfPixel(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (PLAYER_HEAD_MARKER_SIZE / 2.0F));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, marker.alpha());
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(markerX, markerY, 0.0F);
        guiGraphics.blit(
                marker.skinTexture(),
                0,
                0,
                PLAYER_HEAD_MARKER_SIZE,
                PLAYER_HEAD_MARKER_SIZE,
                PLAYER_HEAD_U,
                PLAYER_HEAD_V,
                8,
                8,
                PLAYER_HEAD_TEXTURE_SIZE,
                PLAYER_HEAD_TEXTURE_SIZE
        );
        guiGraphics.blit(
                marker.skinTexture(),
                0,
                0,
                PLAYER_HEAD_MARKER_SIZE,
                PLAYER_HEAD_MARKER_SIZE,
                PLAYER_HEAD_HAT_U,
                PLAYER_HEAD_HAT_V,
                8,
                8,
                PLAYER_HEAD_TEXTURE_SIZE,
                PLAYER_HEAD_TEXTURE_SIZE
        );
        guiGraphics.pose().popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
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
        int color = colorFromWaypointId(waypointId);
        float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        markers.add(new WaypointMarker(waypointId, wrapTo180(directionYaw), color, index));
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
            PlayerSkin playerSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(otherPlayer.getGameProfile());
            markers.add(new PlayerHeadMarker(playerSkin.texture(), wrapTo180(directionYaw), alpha));
        }
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
            return 1.0F - (progress * (1.0F - PLAYER_MIN_ALPHA));
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

    private record WaypointMarker(UUID waypointId, float directionYaw, int rgbColor, int index) {
    }

    private record PlayerHeadMarker(ResourceLocation skinTexture, float directionYaw, float alpha) {
    }
}