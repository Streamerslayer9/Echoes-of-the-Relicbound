package com.relicbound.core;

import com.relicbound.RelicboundMod;
import com.relicbound.item.ResonanceForkItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RelicboundMod.MOD_ID);

    public static final RegistryObject<Item> RESONANCE_FORK = ITEMS.register("resonance_fork",
            () -> new ResonanceForkItem(new Item.Properties().stacksTo(1)));

    // Relics (Felt-Padded Core, Echoic Lens, Resonant Tuning Ring) arrive in Milestone 5.

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
