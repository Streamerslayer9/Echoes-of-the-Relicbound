package com.relicbound;

import com.mojang.logging.LogUtils;
import com.relicbound.core.ModBlocks;
import com.relicbound.core.ModEntities;
import com.relicbound.core.ModItems;
import com.relicbound.core.ModPackets;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RelicboundMod.MOD_ID)
public class RelicboundMod {
    public static final String MOD_ID = "relicbound";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RelicboundMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModEntities.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::addCreativeTabContents);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModPackets::register);
        LOGGER.info("Relicbound common setup complete");
    }

    private void addCreativeTabContents(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.RESONANCE_FORK);
        }
    }
}
