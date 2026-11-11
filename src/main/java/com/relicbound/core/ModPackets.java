package com.relicbound.core;

import com.relicbound.RelicboundMod;
import com.relicbound.sound.SoundWavePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModPackets {
    private static final String PROTOCOL_VERSION = "1";

    /** Clients within this many blocks of a wave's origin receive its packets. */
    public static final double BROADCAST_RADIUS = 64.0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RelicboundMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int nextId = 0;

    private ModPackets() {}

    public static void register() {
        CHANNEL.registerMessage(nextId++, SoundWavePacket.class,
                SoundWavePacket::encode, SoundWavePacket::decode, SoundWavePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /** Sends a packet to every player near {@code center} in the given level. */
    public static void sendToNearby(ServerLevel level, BlockPos center, Object packet) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                BROADCAST_RADIUS, level.dimension())), packet);
    }
}
