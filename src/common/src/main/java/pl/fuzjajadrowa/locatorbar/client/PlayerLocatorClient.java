package pl.fuzjajadrowa.locatorbar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
//? if >=1.20.2
import net.minecraft.world.entity.player.PlayerSkin;
//? if <1.20.2
/*import net.minecraft.client.player.AbstractClientPlayer;*/
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.fuzjajadrowa.locatorbar.network.PlayerLocatorPayload;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerLocatorClient {
    private static final long MAX_PAYLOAD_AGE_MILLIS = 2000L;
    private static List<PlayerLocatorPayload.Entry> entries = List.of();
    private static long lastUpdateMillis;

    private PlayerLocatorClient() {
    }

    public static void apply(PlayerLocatorPayload payload) {
        entries = List.copyOf(payload.entries());
        lastUpdateMillis = System.currentTimeMillis();
    }

    public static void clear() {
        entries = List.of();
        lastUpdateMillis = 0L;
    }

    public static List<Marker> collectMarkers(Player localPlayer, AlphaFunction alphaFunction) {
        if (entries.isEmpty() || System.currentTimeMillis() - lastUpdateMillis > MAX_PAYLOAD_AGE_MILLIS) {
            return collectEntityMarkers(localPlayer, alphaFunction);
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return List.of();
        }

        List<Marker> markers = new ArrayList<>();
        for (PlayerLocatorPayload.Entry entry : entries) {
            double dx = entry.x() - localPlayer.getX();
            double dz = entry.z() - localPlayer.getZ();
            if (dx * dx + dz * dz < 1.0E-6D) {
                continue;
            }

            float distance = (float) Math.sqrt(dx * dx + dz * dz);
            float alpha = alphaFunction.compute(distance);
            if (alpha <= 0.0F) {
                continue;
            }

            PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(entry.playerId());
            if (playerInfo == null) {
                continue;
            }

            //? if >=1.21.11 {
            PlayerSkin playerSkin = playerInfo.getSkin();
            Identifier skinTexture = skinTexture(playerSkin);
            //?} elif >=1.20.2 {
            /*PlayerSkin playerSkin = playerInfo.getSkin();
            Identifier skinTexture = skinTexture(playerSkin);
            *///?} else {
            /*Identifier skinTexture = playerInfo.getSkinLocation();
            *///?}
            float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            //? if >=26.2 {
            String playerName = playerInfo.getProfile().name();
            //?} elif >=1.21.11 {
            String playerName = playerInfo.getProfile().name();
            //?} else {
            /*String playerName = playerInfo.getProfile().getName();
            */
            //?}
            //? if >=26.2 {
            net.minecraft.world.scores.PlayerTeam team = localPlayer.level().getScoreboard().getPlayersTeam(playerName);
            Integer teamColor = (team != null && team.getColor().isPresent()) ? team.getColor().get().rgb() : null;
            //?} else {
            /*net.minecraft.world.scores.PlayerTeam team = localPlayer.level().getScoreboard().getPlayersTeam(playerName);
            Integer teamColor = (team != null && team.getColor() != null && team.getColor().getColor() != null) ? team.getColor().getColor() : null;
            */
            //?}
            markers.add(new Marker(entry.playerId(), skinTexture, directionYaw, alpha, distance, teamColor));
        }

        markers.sort(Comparator.comparingDouble(Marker::distance));
        return markers;
    }

    private static List<Marker> collectEntityMarkers(Player localPlayer, AlphaFunction alphaFunction) {
        List<Marker> markers = new ArrayList<>();
        for (Player otherPlayer : localPlayer.level().players()) {
            if (otherPlayer == localPlayer || shouldHidePlayerHead(localPlayer, otherPlayer)) {
                continue;
            }

            double dx = otherPlayer.getX() - localPlayer.getX();
            double dz = otherPlayer.getZ() - localPlayer.getZ();
            if (dx * dx + dz * dz < 1.0E-6D) {
                continue;
            }

            float distance = (float) Math.sqrt(dx * dx + dz * dz);
            float alpha = alphaFunction.compute(distance);
            if (alpha <= 0.0F) {
                continue;
            }

            //? if >=1.21.11 {
            PlayerSkin playerSkin = Minecraft.getInstance().getSkinManager().createLookup(otherPlayer.getGameProfile(), false).get();
            Identifier skinTexture = skinTexture(playerSkin);
            //?} elif >=1.20.2 {
            /*PlayerSkin playerSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(otherPlayer.getGameProfile());
            Identifier skinTexture = skinTexture(playerSkin);
            *///?} else {
            /*Identifier skinTexture = otherPlayer instanceof AbstractClientPlayer ? ((AbstractClientPlayer) otherPlayer).getSkinTextureLocation() : net.minecraft.client.resources.DefaultPlayerSkin.getDefaultSkin(otherPlayer.getUUID());
            *///?}
            float directionYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            //? if >=26.2 {
            net.minecraft.world.scores.Team team = otherPlayer.getTeam();
            Integer teamColor = (team != null && team.getColor().isPresent()) ? team.getColor().get().rgb() : null;
            //?} else {
            /*net.minecraft.world.scores.Team team = otherPlayer.getTeam();
            Integer teamColor = (team != null && team.getColor() != null && team.getColor().getColor() != null) ? team.getColor().getColor() : null;
            */
            //?}
            markers.add(new Marker(otherPlayer.getUUID(), skinTexture, directionYaw, alpha, distance, teamColor));
        }

        markers.sort(Comparator.comparingDouble(Marker::distance));
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

    //? if >=1.20.2 {
    private static Identifier skinTexture(PlayerSkin playerSkin) {
        //? if >=1.21.11
        return playerSkin.body().texturePath();
        //? if <1.21.11
        /*return playerSkin.texture();*/
    }
    //?}

    @FunctionalInterface
    public interface AlphaFunction {
        float compute(float distance);
    }

    public record Marker(UUID playerId, Identifier skinTexture, float directionYaw, float alpha, float distance, Integer teamColor) {
    }
}