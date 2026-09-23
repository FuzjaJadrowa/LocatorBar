package pl.fuzjajadrowa.locatorbar.network;

//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}
import net.minecraft.resources.Identifier;
import pl.fuzjajadrowa.locatorbar.LocatorBar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//? if >=1.20.5
public record PlayerLocatorPayload(List<Entry> entries) implements CustomPacketPayload {
//? if <1.20.5
/*public record PlayerLocatorPayload(List<Entry> entries) {*/

    //? if >=1.20.5 {
    public static final Type<PlayerLocatorPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(LocatorBar.MOD_ID, "player_locator"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerLocatorPayload> STREAM_CODEC = StreamCodec.ofMember(
            PlayerLocatorPayload::write,
            PlayerLocatorPayload::read
    );

    private static PlayerLocatorPayload read(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        validateEntryCount(size);
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new Entry(buffer.readUUID(), buffer.readDouble(), buffer.readDouble()));
        }
        return new PlayerLocatorPayload(List.copyOf(entries));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(entries.size());
        for (Entry entry : entries) {
            buffer.writeUUID(entry.playerId());
            buffer.writeDouble(entry.x());
            buffer.writeDouble(entry.z());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?}

    public static void validateEntryCount(int count) {
        if (count < 0 || count > 64) {
            throw new IllegalArgumentException("Invalid player marker count: " + count);
        }
    }

    public record Entry(UUID playerId, double x, double z) {
    }
}
