package pl.fuzjajadrowa.locatorbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.waypoint.LodestoneWaypointData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private static final int WAYPOINT_MARKER_SIZE = BAR_TEXTURE_HEIGHT + 12;
    private static final int WAYPOINT_TOP_OVERFLOW = 8;
    private static final int WAYPOINT_BOTTOM_OVERFLOW = 4;
    private static final int PLAYER_HEAD_MARKER_SIZE = WAYPOINT_MARKER_SIZE;
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
        int waypointMarkerY = y - WAYPOINT_TOP_OVERFLOW;
        int directionMarkerY = y - DIRECTION_OVERFLOW + ((BAR_TEXTURE_HEIGHT + (DIRECTION_OVERFLOW * 2) - DIRECTION_MARKER_SIZE) / 2);
        int headMarkerY = y - PLAYER_HEAD_OVERFLOW + ((BAR_TEXTURE_HEIGHT + (PLAYER_HEAD_OVERFLOW * 2) - PLAYER_HEAD_MARKER_SIZE) / 2);
        int scissorTopOverflow = Math.max(Math.max(DIRECTION_OVERFLOW, PLAYER_HEAD_OVERFLOW), WAYPOINT_TOP_OVERFLOW);
        int scissorBottomOverflow = Math.max(Math.max(DIRECTION_OVERFLOW, PLAYER_HEAD_OVERFLOW), WAYPOINT_BOTTOM_OVERFLOW);

        guiGraphics.enableScissor(x, y - scissorTopOverflow, x + BAR_TEXTURE_WIDTH, y + BAR_TEXTURE_HEIGHT + scissorBottomOverflow);
        renderDirectionMarker(guiGraphics, NORTH, 180.0F, yaw, centerX, directionMarkerY);
        renderDirectionMarker(guiGraphics, SOUTH, 0.0F, yaw, centerX, directionMarkerY);
        renderDirectionMarker(guiGraphics, EAST, -90.0F, yaw, centerX, directionMarkerY);
        renderDirectionMarker(guiGraphics, WEST, 90.0F, yaw, centerX, directionMarkerY);

        for (WaypointMarker marker : collectWaypointMarkers(player)) {
            renderWaypointMarker(guiGraphics, minecraft, marker, yaw, centerX, waypointMarkerY);
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
        int markerX = Math.round(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (DIRECTION_MARKER_SIZE / 2.0F));

        guiGraphics.blit(
                texture,
                markerX,
                markerY,
                DIRECTION_MARKER_SIZE,
                DIRECTION_MARKER_SIZE,
                ICON_MARGIN,
                ICON_MARGIN,
                ICON_DOT_SIZE,
                ICON_DOT_SIZE,
                ICON_TEXTURE_SIZE,
                ICON_TEXTURE_SIZE
        );
    }

    private static void renderWaypointMarker(
            GuiGraphics guiGraphics,
            Minecraft minecraft,
            WaypointMarker marker,
            float playerYaw,
            float centerX,
            int markerY
    ) {
        float relative = wrapTo180(marker.directionYaw() - playerYaw);
        if (Math.abs(relative) > HALF_VIEW_ANGLE) {
            return;
        }

        float normalized = relative / HALF_VIEW_ANGLE;
        int markerX = Math.round(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (WAYPOINT_MARKER_SIZE / 2.0F));

        float red = ((marker.color() >> 16) & 0xFF) / 255.0F;
        float green = ((marker.color() >> 8) & 0xFF) / 255.0F;
        float blue = (marker.color() & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        guiGraphics.blit(
                WAYPOINT,
                markerX,
                markerY,
                WAYPOINT_MARKER_SIZE,
                WAYPOINT_MARKER_SIZE,
                ICON_MARGIN,
                ICON_MARGIN,
                ICON_DOT_SIZE,
                ICON_DOT_SIZE,
                ICON_TEXTURE_SIZE,
                ICON_TEXTURE_SIZE
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        String label = Integer.toString(marker.id());
        int textColor = hasBrightColor(marker.color()) ? 0x101010 : 0xFFFFFF;
        int textX = markerX + (WAYPOINT_MARKER_SIZE - minecraft.font.width(label)) / 2;
        int textY = markerY + (WAYPOINT_MARKER_SIZE - minecraft.font.lineHeight) / 2;
        guiGraphics.drawString(minecraft.font, label, textX, textY, textColor, false);
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
        int markerX = Math.round(centerX + normalized * (BAR_TEXTURE_WIDTH / 2.0F) - (PLAYER_HEAD_MARKER_SIZE / 2.0F));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, marker.alpha());
        guiGraphics.blit(
                marker.skinTexture(),
                markerX,
                markerY,
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
                markerX,
                markerY,
                PLAYER_HEAD_MARKER_SIZE,
                PLAYER_HEAD_MARKER_SIZE,
                PLAYER_HEAD_HAT_U,
                PLAYER_HEAD_HAT_V,
                8,
                8,
                PLAYER_HEAD_TEXTURE_SIZE,
                PLAYER_HEAD_TEXTURE_SIZE
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static List<WaypointMarker> collectWaypointMarkers(Player localPlayer) {
        UUID playerId = localPlayer.getUUID();
        Inventory inventory = localPlayer.getInventory();
        Map<Integer, WaypointMarker> waypointById = new LinkedHashMap<>();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            Optional<LodestoneWaypointData.WaypointInfo> optionalInfo = LodestoneWaypointData.getOwnedWaypoint(inventory.getItem(slot), playerId);
            if (optionalInfo.isEmpty()) {
                continue;
            }

            LodestoneWaypointData.WaypointInfo info = optionalInfo.get();
            if (!info.target().dimension().equals(localPlayer.level().dimension())) {
                continue;
            }

            double dx = info.target().pos().getX() + 0.5D - localPlayer.getX();
            double dz = info.target().pos().getZ() + 0.5D - localPlayer.getZ();
            if (dx * dx + dz * dz < 1.0E-6D) {
                continue;
            }

            float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            waypointById.putIfAbsent(
                    info.id(),
                    new WaypointMarker(info.id(), wrapTo180(directionYaw), waypointColor(info.owner(), info.id()))
            );
        }

        return new ArrayList<>(waypointById.values());
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

    private static int waypointColor(UUID owner, int waypointId) {
        int hash = owner.hashCode() * 31 + waypointId * 374761393;
        hash ^= hash >>> 13;
        hash *= 1274126177;
        hash ^= hash >>> 16;

        float hue = (hash & 0xFFFF) / 65535.0F;
        float saturation = 0.65F + (((hash >>> 16) & 0xFF) / 255.0F) * 0.25F;
        float value = 0.85F + (((hash >>> 24) & 0x7F) / 127.0F) * 0.10F;
        return Mth.hsvToRgb(hue, saturation, value);
    }

    private static boolean hasBrightColor(int color) {
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float luminance = 0.2126F * red + 0.7152F * green + 0.0722F * blue;
        return luminance > 0.6F;
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

    private record PlayerHeadMarker(ResourceLocation skinTexture, float directionYaw, float alpha) {
    }

    private record WaypointMarker(int id, float directionYaw, int color) {
    }
}