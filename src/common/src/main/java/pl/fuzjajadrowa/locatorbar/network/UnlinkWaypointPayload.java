package pl.fuzjajadrowa.locatorbar.network;

//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}
import net.minecraft.resources.Identifier;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

import java.util.UUID;

//? if >=1.20.5
public record UnlinkWaypointPayload(UUID waypointId) implements CustomPacketPayload {
//? if <1.20.5
/*public record UnlinkWaypointPayload(UUID waypointId) {*/

    //? if >=1.20.5 {
    public static final Type<UnlinkWaypointPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(LocatorBar.MOD_ID, "unlink_waypoint"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnlinkWaypointPayload> STREAM_CODEC = StreamCodec.ofMember(
            UnlinkWaypointPayload::write,
            UnlinkWaypointPayload::read
    );

    private static UnlinkWaypointPayload read(RegistryFriendlyByteBuf buffer) {
        return new UnlinkWaypointPayload(buffer.readUUID());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(waypointId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}
}