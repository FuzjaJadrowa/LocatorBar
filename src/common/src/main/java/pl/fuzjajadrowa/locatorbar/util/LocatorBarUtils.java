package pl.fuzjajadrowa.locatorbar.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.util.Mth;

import java.util.UUID;

public final class LocatorBarUtils {
    private LocatorBarUtils() {
    }

    public static float wrapTo180(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        } else if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    public static float quantizeToHalfPixel(float value) {
        return Math.round(value * 2.0F) / 2.0F;
    }

    public static int colorFromId(UUID id, float saturationMin, float saturationRange, float valueMin, float valueRange) {
        long hash = id.getMostSignificantBits() ^ id.getLeastSignificantBits();
        float hue = (hash & 0xFFFFL) / 65535.0F;
        float saturation = saturationMin + (((hash >>> 16) & 0xFFL) / 255.0F) * saturationRange;
        float value = valueMin + (((hash >>> 24) & 0xFFL) / 255.0F) * valueRange;
        return Mth.hsvToRgb(hue, saturation, value);
    }

    public static boolean shouldHidePlayerHead(Player observer, Player target) {
        if (!target.level().dimension().equals(observer.level().dimension())) {
            return true;
        }
        if (target.isCrouching()) {
            return true;
        }

        ItemStack helmet = target.getItemBySlot(EquipmentSlot.HEAD);
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

    public static int setBrightness(int color, float brightness) {
        int r = (int) (((color >> 16) & 0xFF) * brightness);
        int g = (int) (((color >> 8) & 0xFF) * brightness);
        int b = (int) ((color & 0xFF) * brightness);
        int a = (color >> 24) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int colorFromPlayerId(UUID id) {
        //? if >=26.2 {
        int color = (255 << 24) | (id.hashCode() & 0x00FFFFFF);
        return setBrightness(color, 0.9F);
        //?} else {
        /*return colorFromId(id, 0.70F, 0.20F, 0.85F, 0.15F);
        *///?}
    }
}