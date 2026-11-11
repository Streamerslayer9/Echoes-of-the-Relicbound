package com.relicbound.sound;

import com.relicbound.client.ClientWaveRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * S2C packet carrying the wave nodes the server confirmed during one tick.
 * Clients only read it to spawn particles.
 */
public record SoundWavePacket(UUID waveId, List<Entry> entries) {
    public static final int MAX_ENTRIES = 512;

    /** @param direction travel direction's 3D data value, or -1 for the origin node */
    public record Entry(long pos, byte direction, float energy) {}

    public static void encode(SoundWavePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.waveId);
        buf.writeVarInt(msg.entries.size());
        for (Entry e : msg.entries) {
            buf.writeLong(e.pos());
            buf.writeByte(e.direction());
            buf.writeFloat(e.energy());
        }
    }

    public static SoundWavePacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_ENTRIES) {
            throw new IllegalArgumentException("Invalid SoundWavePacket size: " + count);
        }
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(buf.readLong(), buf.readByte(), buf.readFloat()));
        }
        return new SoundWavePacket(id, entries);
    }

    public static void handle(SoundWavePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWaveRenderer.handle(msg)));
        ctx.setPacketHandled(true);
    }
}
