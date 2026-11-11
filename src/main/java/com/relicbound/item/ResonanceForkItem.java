package com.relicbound.item;

import com.relicbound.RelicboundMod;
import com.relicbound.sound.SoundPropagationSimulator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

        if (level instanceof ServerLevel serverLevel) {
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            BlockPos origin = BlockPos.containing(player.getEyePosition());
            SoundPropagationSimulator.simulateWave(serverLevel, origin, INITIAL_ENERGY, MAX_RADIUS);

            serverLevel.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.8f, 1.5f);
            RelicboundMod.LOGGER.debug("Resonance Fork pulse started by {} at {}",
                    player.getName().getString(), origin);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
