package pl.fuzjajadrowa.locatorbar.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.client.PlayerLocatorClient;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;
import pl.fuzjajadrowa.locatorbar.network.PlayerLocatorPayload;
import pl.fuzjajadrowa.locatorbar.network.ServerConfigPayload;
import pl.fuzjajadrowa.locatorbar.network.UnlinkWaypointPayload;
import pl.fuzjajadrowa.locatorbar.server.PlayerLocatorBroadcaster;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointData;
import pl.fuzjajadrowa.locatorbar.waypoint.WaypointInventory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class LocatorBarFabricNetworking {
    private static int locatorUpdateTick;
    private static MinecraftServer currentServer;

    private LocatorBarFabricNetworking() {
    }

    public static void initCommon() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);

        LocatorBar.setBroadcaster(settings -> {
            if (currentServer != null) {
                for (ServerPlayer player : currentServer.getPlayerList().getPlayers()) {
                    if (ServerPlayNetworking.canSend(player, ServerConfigPayload.TYPE)) {
                        ServerPlayNetworking.send(player, new ServerConfigPayload(settings));
                    }
                }
            }
        });

        //? if >=26.1 {
        PayloadTypeRegistry.clientboundPlay().register(ServerConfigPayload.TYPE, ServerConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerLocatorPayload.TYPE, PlayerLocatorPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UnlinkWaypointPayload.TYPE, UnlinkWaypointPayload.STREAM_CODEC);
        //?} else {
        /*PayloadTypeRegistry.playS2C().register(ServerConfigPayload.TYPE, ServerConfigPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerLocatorPayload.TYPE, PlayerLocatorPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UnlinkWaypointPayload.TYPE, UnlinkWaypointPayload.STREAM_CODEC);
        *///?}
        ServerPlayNetworking.registerGlobalReceiver(UnlinkWaypointPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> WaypointData.unlinkWaypoint(player, payload.waypointId()));
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            WaypointInventory.migrate(handler.getPlayer());
            var settings = LocatorBarServerConfig.get();
            if (settings != null && ServerPlayNetworking.canSend(handler, ServerConfigPayload.TYPE)) {
                sender.sendPacket(new ServerConfigPayload(settings));
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            locatorUpdateTick++;
            if (locatorUpdateTick < PlayerLocatorBroadcaster.UPDATE_INTERVAL_TICKS) {
                return;
            }
            locatorUpdateTick = 0;

            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer player : players) {
                WaypointInventory.migrate(player);
            }

            var settings = LocatorBarServerConfig.get();
            if (settings != null && settings.playerMarkerType() == PlayerMarkerType.OFF) {
                return;
            }

            for (ServerPlayer player : players) {
                if (ServerPlayNetworking.canSend(player, PlayerLocatorPayload.TYPE)) {
                    ServerPlayNetworking.send(player, PlayerLocatorBroadcaster.createPayload(player, players));
                }
            }
        });
    }

    public static void initClient() {
        LocatorBar.setUnlinkWaypointSender(waypointId -> {
            if (ClientPlayNetworking.canSend(UnlinkWaypointPayload.TYPE)) {
                ClientPlayNetworking.send(new UnlinkWaypointPayload(waypointId));
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(ServerConfigPayload.TYPE, (payload, context) -> {
            LocatorBarConfig.applyServerSettings(payload.settings());
        });
        ClientPlayNetworking.registerGlobalReceiver(PlayerLocatorPayload.TYPE, (payload, context) -> {
            PlayerLocatorClient.apply(payload);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LocatorBarConfig.clearServerSettings();
            PlayerLocatorClient.clear();
        });
    }
}