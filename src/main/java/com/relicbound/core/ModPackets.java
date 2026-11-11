package com.relicbound.core;

import com.relicbound.RelicboundMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModPackets {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RelicboundMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private ModPackets() {}

    /** Packet registration happens here. SoundWavePacket is added in Milestone 2. */
    public static void register() {
        // intentionally empty for Milestone 1
    }
}
