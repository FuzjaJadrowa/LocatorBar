package pl.fuzjajadrowa.locatorbar.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import pl.fuzjajadrowa.locatorbar.client.PlayerLocatorClient;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;
import pl.fuzjajadrowa.locatorbar.network.PlayerLocatorPayload;
import pl.fuzjajadrowa.locatorbar.network.ServerConfigPayload;
import pl.fuzjajadrowa.locatorbar.server.PlayerLocatorBroadcaster;

import java.util.List;

public final class LocatorBarNeoForgeNetworking {
    private static int locatorUpdateTick;

    private LocatorBarNeoForgeNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(ServerConfigPayload.TYPE, ServerConfigPayload.STREAM_CODEC, (payload, context) -> {
            LocatorBarConfig.applyServerSettings(payload.settings());
        });
        registrar.playToClient(PlayerLocatorPayload.TYPE, PlayerLocatorPayload.STREAM_CODEC, (payload, context) -> {
            PlayerLocatorClient.apply(payload);
        });
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var settings = LocatorBarServerConfig.get();
        if (settings != null && event.getEntity() instanceof ServerPlayer player && player.connection.hasChannel(ServerConfigPayload.TYPE)) {
            PacketDistributor.sendToPlayer(player, new ServerConfigPayload(settings));
        }
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        locatorUpdateTick++;
        if (locatorUpdateTick < PlayerLocatorBroadcaster.UPDATE_INTERVAL_TICKS) {
            return;
        }
        locatorUpdateTick = 0;

        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            if (player.connection.hasChannel(PlayerLocatorPayload.TYPE)) {
                PacketDistributor.sendToPlayer(player, PlayerLocatorBroadcaster.createPayload(player, players));
            }
        }
    }

    public static void broadcastConfig(LocatorBarServerConfig.ServerSettings settings) {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        var payload = new ServerConfigPayload(settings);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.connection.hasChannel(ServerConfigPayload.TYPE)) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
