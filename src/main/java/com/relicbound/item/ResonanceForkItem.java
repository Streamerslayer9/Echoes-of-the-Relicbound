package com.relicbound.item;

import com.relicbound.RelicboundMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ResonanceForkItem extends Item {
    public static final int COOLDOWN_TICKS = 20;
    public static final float INITIAL_ENERGY = 15.0f;
    public static final int MAX_RADIUS = 16;

    public ResonanceForkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            // Milestone 1: debug only. Milestone 2 replaces this with
            // SoundPropagationSimulator.simulateWave(...) on the server thread.
            player.sendSystemMessage(Component.literal(String.format(
                    "[Relicbound] Resonance Fork struck at %.1f, %.1f, %.1f (energy %.1f, radius %d)",
                    player.getX(), player.getY(), player.getZ(), INITIAL_ENERGY, MAX_RADIUS)));
            RelicboundMod.LOGGER.info("Resonance Fork used by {}", player.getName().getString());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
