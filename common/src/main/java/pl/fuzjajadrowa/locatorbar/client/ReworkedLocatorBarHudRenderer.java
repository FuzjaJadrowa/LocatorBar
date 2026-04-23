package pl.fuzjajadrowa.locatorbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

import java.util.ArrayList;
import java.util.List;

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
    private static final int PLAYER_HEAD_MARKER_SIZE = 12;
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
        int scissorOverflow = Math.max(DIRECTION_OVERFLOW, PLAYER_HEAD_OVERFLOW);

        guiGraphics.enableScissor(x, y - scissorOverflow, x + BAR_TEXTURE_WIDTH, y + BAR_TEXTURE_HEIGHT + scissorOverflow);
        renderDirectionMarker(guiGraphics, NORTH, 180.0F, yaw, centerX, directionMarkerY);
        renderDirectionMarker(guiGraphics, SOUTH, 0.0F, yaw, centerX, directionMarkerY);
        renderDirectionMarker(guiGraphics, EAST, -90.0F, yaw, centerX, directionMarkerY);
        renderDirectionMarker(guiGraphics, WEST, 90.0F, yaw, centerX, directionMarkerY);

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
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, marker.alpha());
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
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static List<PlayerHeadMarker> collectPlayerHeadMarkers(Player localPlayer) {
        List<PlayerHeadMarker> markers = new ArrayList<>();
        for (Player otherPlayer : localPlayer.level().players()) {
            if (!(otherPlayer instanceof AbstractClientPlayer clientPlayer) || otherPlayer == localPlayer) {
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
            PlayerSkin playerSkin = clientPlayer.getSkin();
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
        float distance = localPlayer.distanceTo(otherPlayer);
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
}