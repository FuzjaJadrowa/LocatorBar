package pl.fuzjajadrowa.locatorbar.network;

//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}
import net.minecraft.resources.Identifier;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings;

//? if >=1.20.5
public record ServerConfigPayload(ServerSettings settings) implements CustomPacketPayload {
//? if <1.20.5
/*public record ServerConfigPayload(ServerSettings settings) {*/

    //? if >=1.20.5 {
    public static final Type<ServerConfigPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(LocatorBar.MOD_ID, "server_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfigPayload> STREAM_CODEC = StreamCodec.ofMember(
            ServerConfigPayload::write,
            ServerConfigPayload::read
    );

    private static ServerConfigPayload read(RegistryFriendlyByteBuf buffer) {
        return new ServerConfigPayload(new ServerSettings(
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

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(settings.style().ordinal());
        buffer.writeBoolean(settings.showCoordinates());
        buffer.writeBoolean(settings.showDays());
        buffer.writeBoolean(settings.showWorldDirections());
        buffer.writeVarInt(settings.playerMarkerType().ordinal());
        buffer.writeVarInt(settings.maxVisiblePlayers());
        buffer.writeFloat(settings.playerMarkerFadeStartDistance());
        buffer.writeFloat(settings.playerMarkerFadeToMinDistance());
        buffer.writeFloat(settings.playerMarkerHideDistance());
        buffer.writeFloat(settings.playerMarkerMinAlphaPercent());
        buffer.writeBoolean(settings.showWaypoints());
        buffer.writeVarInt(settings.maxVisibleWaypoints());
        buffer.writeBoolean(settings.showDeathWaypoint());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}