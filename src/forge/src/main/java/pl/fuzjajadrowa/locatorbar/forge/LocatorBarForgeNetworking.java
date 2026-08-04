package pl.fuzjajadrowa.locatorbar.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.client.PlayerLocatorClient;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings;
import pl.fuzjajadrowa.locatorbar.server.PlayerLocatorBroadcaster;
import pl.fuzjajadrowa.locatorbar.network.PlayerLocatorPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class LocatorBarForgeNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LocatorBar.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int locatorUpdateTick;

    private LocatorBarForgeNetworking() {
    }

    public static void register(FMLCommonSetupEvent event) {
        int id = 0;
        INSTANCE.registerMessage(
                id++,
                ServerConfigPacket.class,
                ServerConfigPacket::encode,
                ServerConfigPacket::decode,
                ServerConfigPacket::handle
        );
        INSTANCE.registerMessage(
                id++,
                PlayerLocatorPacket.class,
                PlayerLocatorPacket::encode,
                PlayerLocatorPacket::decode,
                PlayerLocatorPacket::handle
        );
    }

    public static class ServerConfigPacket {
        private final ServerSettings settings;

        public ServerConfigPacket(ServerSettings settings) {
            this.settings = settings;
        }

        public static void encode(ServerConfigPacket msg, FriendlyByteBuf buffer) {
            buffer.writeVarInt(msg.settings.style().ordinal());
            buffer.writeBoolean(msg.settings.showCoordinates());
            buffer.writeBoolean(msg.settings.showDays());
            buffer.writeBoolean(msg.settings.showWorldDirections());
            buffer.writeVarInt(msg.settings.playerMarkerType().ordinal());
            buffer.writeVarInt(msg.settings.maxVisiblePlayers());
            buffer.writeFloat(msg.settings.playerMarkerFadeStartDistance());
            buffer.writeFloat(msg.settings.playerMarkerFadeToMinDistance());
            buffer.writeFloat(msg.settings.playerMarkerHideDistance());
            buffer.writeFloat(msg.settings.playerMarkerMinAlphaPercent());
            buffer.writeBoolean(msg.settings.showWaypoints());
            buffer.writeVarInt(msg.settings.maxVisibleWaypoints());
            buffer.writeBoolean(msg.settings.showDeathWaypoint());
        }

        public static ServerConfigPacket decode(FriendlyByteBuf buffer) {
            return new ServerConfigPacket(new ServerSettings(
                    LocatorBarStyle.values()[buffer.readVarInt()],
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    PlayerMarkerType.values()[buffer.readVarInt()],
                    buffer.readVarInt(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readBoolean(),
                    buffer.readVarInt(),
                    buffer.readBoolean()
            ));
        }

        public static void handle(ServerConfigPacket msg, Supplier<NetworkEvent.Context> ctxGetter) {
            NetworkEvent.Context ctx = ctxGetter.get();
            ctx.enqueueWork(() -> {
                LocatorBarConfig.applyServerSettings(msg.settings);
            });
            ctx.setPacketHandled(true);
        }
    }

    public static class PlayerLocatorPacket {
        private final List<PlayerLocatorPayload.Entry> entries;

        public PlayerLocatorPacket(List<PlayerLocatorPayload.Entry> entries) {
            this.entries = entries;
        }

        public static void encode(PlayerLocatorPacket msg, FriendlyByteBuf buffer) {
            buffer.writeVarInt(msg.entries.size());
            for (PlayerLocatorPayload.Entry entry : msg.entries) {
                buffer.writeUUID(entry.playerId());
                buffer.writeDouble(entry.x());
                buffer.writeDouble(entry.z());
            }
        }

        public static PlayerLocatorPacket decode(FriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            List<PlayerLocatorPayload.Entry> entries = new ArrayList<>(count);
            for (int idx = 0; idx < count; ++idx) {
                entries.add(new PlayerLocatorPayload.Entry(buffer.readUUID(), buffer.readDouble(), buffer.readDouble()));
            }
            return new PlayerLocatorPacket(entries);
        }

        public static void handle(PlayerLocatorPacket msg, Supplier<NetworkEvent.Context> ctxGetter) {
            NetworkEvent.Context ctx = ctxGetter.get();
            ctx.enqueueWork(() -> {
                PlayerLocatorClient.apply(new PlayerLocatorPayload(msg.entries));
            });
            ctx.setPacketHandled(true);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var settings = LocatorBarServerConfig.get();
        if (settings != null && event.getEntity() instanceof ServerPlayer player) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ServerConfigPacket(settings));
        }
    }

    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var settings = LocatorBarServerConfig.get();
        if (settings != null && event.getEntity() instanceof ServerPlayer player) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ServerConfigPacket(settings));
        }
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        locatorUpdateTick++;
        if (locatorUpdateTick < PlayerLocatorBroadcaster.UPDATE_INTERVAL_TICKS) {
            return;
        }
        locatorUpdateTick = 0;

        List<ServerPlayer> players = event.getServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            PlayerLocatorPayload payload = PlayerLocatorBroadcaster.createPayload(player, players);
            if (payload != null) {
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PlayerLocatorPacket(payload.entries()));
            }
        }
    }
}