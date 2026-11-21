package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;

public final class ClassicExperienceBarState {
    private static final long EXPERIENCE_BAR_VISIBLE_MILLIS = 5000L;
    private static int lastTotalExperience = -1;
    private static long experienceBarVisibleUntil;

    private ClassicExperienceBarState() {
    }

    public static boolean shouldShowVanillaExperienceBar(Minecraft minecraft, Player player) {
        update(player);
        if (isJumpBarActive(player)) {
            return true;
        }
        return isVanillaExperienceBarVisible(minecraft, player) && System.currentTimeMillis() < experienceBarVisibleUntil;
    }

    public static boolean shouldHideVanillaExperienceBar(Minecraft minecraft) {
        if (LocatorBarConfig.getStyle() != LocatorBarStyle.CLASSIC || LocatorBarConfig.isElementsOnXpBar()) {
            return false;
        }

        Player player = minecraft.player;
        return player != null
                && isVanillaExperienceBarVisible(minecraft, player)
                && !shouldShowVanillaExperienceBar(minecraft, player);
    }

    private static boolean isJumpBarActive(Player player) {
        if (!(player instanceof LocalPlayer localPlayer)) {
            return false;
        }
        if (localPlayer.jumpableVehicle() == null) {
            return false;
        }
        //? if >=1.21.11 {
        return localPlayer.getJumpRidingScale() > 0.0F || localPlayer.jumpableVehicle().getJumpCooldown() > 0;
        //?} else {
        /*return true;
        *///?}
    }

    private static void update(Player player) {
        int totalExperience = player.totalExperience;
        if (lastTotalExperience >= 0 && totalExperience > lastTotalExperience) {
            experienceBarVisibleUntil = System.currentTimeMillis() + EXPERIENCE_BAR_VISIBLE_MILLIS;
        }
        lastTotalExperience = totalExperience;
    }

    private static boolean isVanillaExperienceBarVisible(Minecraft minecraft, Player player) {
        return minecraft.gameMode != null && minecraft.gameMode.hasExperience() && !player.isSpectator();
    }
}