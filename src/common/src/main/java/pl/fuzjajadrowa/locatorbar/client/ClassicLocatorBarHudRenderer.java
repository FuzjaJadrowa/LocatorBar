package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.util.LocatorBarUtils;

public final class ClassicLocatorBarHudRenderer {
    private static final Identifier CLASSIC_LOCATOR_BAR_BACKGROUND = Identifier.fromNamespaceAndPath(
            LocatorBar.MOD_ID,
            "textures/gui/classic_locator_bar_background.png"
    );
    private static final int BAR_TEXTURE_WIDTH = 182;
    private static final int BAR_TEXTURE_HEIGHT = 5;
    private static final float CLASSIC_DIRECTIONS_DEFAULT_SCALE = 0.7F;
    private static final float CLASSIC_PLAYER_HEADS_DEFAULT_SCALE = 0.7F;
    private static final int BASE_DIRECTION_MARKER_SIZE = 12;
    private static final int BASE_DIRECTION_OVERFLOW = 2;
    private static final int BASE_PLAYER_HEAD_MARKER_SIZE = 12;
    private static final int BASE_PLAYER_HEAD_OVERFLOW = 2;
    private static final int WAYPOINT_TEXTURE_SIZE = 36;
    private static final int BASE_WAYPOINT_MARKER_SIZE = 10;
    private static final int WAYPOINT_Y_OFFSET = 2;

    private static final MarkerRenderer.Layout MARKER_LAYOUT = new MarkerRenderer.Layout(5, 10, 0.65F, -1);

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
        //? if <1.21.11 {
        /*guiGraphics.flush();
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
        *///?}

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

        MarkerRenderer.render(guiGraphics, player, yaw, halfViewAngle, centerX,
                directionMarkerY, directionMarkerSize, waypointMarkerY, waypointMarkerSize,
                headMarkerY, playerHeadMarkerSize, MARKER_LAYOUT);

        RenderCompat.pop(guiGraphics);
        guiGraphics.disableScissor();
        //? if <1.21.11 {
        /*com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        guiGraphics.flush();
        *///?}
    }

    private static boolean isVanillaExperienceBarVisible(Minecraft minecraft, Player player) {
        return minecraft.gameMode != null && minecraft.gameMode.hasExperience() && !player.isSpectator();
    }
}