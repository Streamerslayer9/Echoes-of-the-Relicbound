package com.relicbound.sound;

import com.relicbound.RelicboundMod;
import com.relicbound.core.ModPackets;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Server-side wavefront flood fill. Waves are queued by {@link #simulateWave} and advanced
 * once per server tick, with a global cap of {@link #MAX_NODES_PER_TICK} processed nodes.
 */
@Mod.EventBusSubscriber(modid = RelicboundMod.MOD_ID)
public final class SoundPropagationSimulator {
    public static final int MAX_NODES_PER_TICK = 300;
    public static final int MAX_ACTIVE_WAVES = 32;

    public static final float AIR_COST = 0.1f;
    public static final float SOLID_COST = 1.5f;
    public static final float WOOL_COST = 4.0f;

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final List<ActiveWave> ACTIVE_WAVES = new ArrayList<>();

    private SoundPropagationSimulator() {}

    /**
     * Starts a wave. Always runs its work on the server thread.
     *
     * @return the new wave's id
     */
    public static UUID simulateWave(ServerLevel level, BlockPos origin, float initialEnergy, int maxRadius) {
        UUID waveId = UUID.randomUUID();
        BlockPos start = origin.immutable();

        if (!level.getServer().isSameThread()) {
            level.getServer().execute(() -> enqueue(level, start, initialEnergy, maxRadius, waveId));
        } else {
            enqueue(level, start, initialEnergy, maxRadius, waveId);
        }
        return waveId;
    }

    private static void enqueue(ServerLevel level, BlockPos origin, float energy, int radius, UUID waveId) {
        if (ACTIVE_WAVES.size() >= MAX_ACTIVE_WAVES) {
            RelicboundMod.LOGGER.warn("Too many active sound waves, dropping wave {}", waveId);
            return;
        }
        ACTIVE_WAVES.add(new ActiveWave(level, origin, energy, radius, waveId));
    }

    /** Energy lost when a wave enters the given block. */
    public static float attenuation(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.is(BlockTags.WOOL) || state.is(BlockTags.WOOL_CARPETS)) {
            return WOOL_COST;
        }
        if (state.isAir() || state.getCollisionShape(level, pos).isEmpty()) {
            return AIR_COST;
        }
        return SOLID_COST;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE_WAVES.isEmpty()) {
            return;
        }
        int budget = MAX_NODES_PER_TICK;
        Iterator<ActiveWave> it = ACTIVE_WAVES.iterator();
        while (it.hasNext() && budget > 0) {
            ActiveWave wave = it.next();
            budget -= wave.step(budget);
            if (wave.isFinished()) {
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE_WAVES.clear();
    }

    private static final class ActiveWave {
        private final ServerLevel level;
        private final BlockPos origin;
        private final int maxRadiusSq;
        private final UUID id;
        private final ArrayDeque<WaveNode> queue = new ArrayDeque<>();
        /** Best energy this wave has reached each position with. */
        private final Long2FloatOpenHashMap bestEnergy = new Long2FloatOpenHashMap();

        ActiveWave(ServerLevel level, BlockPos origin, float initialEnergy, int maxRadius, UUID id) {
            this.level = level;
            this.origin = origin;
            this.maxRadiusSq = maxRadius * maxRadius;
            this.id = id;
            this.bestEnergy.defaultReturnValue(-1.0f);
            this.bestEnergy.put(origin.asLong(), initialEnergy);
            this.queue.add(new WaveNode(origin, null, initialEnergy, 0, id));
        }

        boolean isFinished() {
            return queue.isEmpty();
        }

        /** Processes up to {@code budget} nodes and returns how many were processed. */
        int step(int budget) {
            List<SoundWavePacket.Entry> confirmed = new ArrayList<>();
            int processed = 0;

            while (processed < budget && !queue.isEmpty()) {
                WaveNode node = queue.poll();
                // A stronger branch reached this position after this node was queued.
                if (bestEnergy.get(node.position().asLong()) > node.energy()) {
                    continue;
                }
                processed++;
                Direction dir = node.incomingDirection();
                confirmed.add(new SoundWavePacket.Entry(
                        node.position().asLong(),
                        (byte) (dir == null ? -1 : dir.get3DDataValue()),
                        node.energy()));
                expand(node);
            }

            if (!confirmed.isEmpty()) {
                ModPackets.sendToNearby(level, origin, new SoundWavePacket(id, confirmed));
            }
            return processed;
        }

        private void expand(WaveNode node) {
            for (Direction dir : DIRECTIONS) {
                BlockPos next = node.position().relative(dir);
                if (next.distSqr(origin) > maxRadiusSq) {
                    continue;
                }
                if (level.isOutsideBuildHeight(next) || !level.isLoaded(next)) {
                    continue;
                }
                float newEnergy = node.energy() - attenuation(level.getBlockState(next), level, next);
                if (newEnergy <= 0.0f) {
                    continue;
                }
                long key = next.asLong();
                if (bestEnergy.get(key) >= newEnergy) {
                    continue; // already reached with equal or greater energy
                }
                bestEnergy.put(key, newEnergy);
                queue.add(node.step(next, dir, newEnergy));
            }
        }
    }
}
