package com.relicbound.core;

import com.relicbound.RelicboundMod;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RelicboundMod.MOD_ID);

    // Acoustic Prism (Milestone 3), Resonance Bricks and Acoustic Receiver (Milestone 5) go here.

    private ModBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
