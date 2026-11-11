package com.relicbound.client;

import com.relicbound.sound.SoundWavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/** Client-only: turns confirmed wave nodes into small expanding particle rings. */
@OnlyIn(Dist.CLIENT)
public final class ClientWaveRenderer {
    /** Upper bound on rings spawned per received packet; extra nodes are skipped evenly. */
    private static final int MAX_RINGS_PER_PACKET = 100;
    private static final int PARTICLES_PER_RING = 4;
    private static final double RING_START_RADIUS = 0.15;
    private static final double RING_SPEED = 0.2;
    private static final double FORWARD_DRIFT = 0.04;

    private ClientWaveRenderer() {}

    public static void handle(SoundWavePacket msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<SoundWavePacket.Entry> entries = msg.entries();
        int stride = Math.max(1, (entries.size() + MAX_RINGS_PER_PACKET - 1) / MAX_RINGS_PER_PACKET);

        for (int i = 0; i < entries.size(); i += stride) {
            SoundWavePacket.Entry entry = entries.get(i);
            BlockPos pos = BlockPos.of(entry.pos());
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.getCollisionShape(level, pos).isEmpty()) {
                continue; // inside a solid block, particles would be invisible
            }
            Direction travel = entry.direction() < 0 ? null : Direction.from3DDataValue(entry.direction());
            spawnRing(level, pos, travel);
        }
    }

    private static void spawnRing(ClientLevel level, BlockPos pos, Direction travel) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        Direction.Axis axis = travel == null ? Direction.Axis.Y : travel.getAxis();
        Vec3 u;
        Vec3 v;
        switch (axis) {
            case X -> { u = new Vec3(0, 1, 0); v = new Vec3(0, 0, 1); }
            case Z -> { u = new Vec3(1, 0, 0); v = new Vec3(0, 1, 0); }
            default -> { u = new Vec3(1, 0, 0); v = new Vec3(0, 0, 1); }
        }

        double fx = travel == null ? 0 : travel.getStepX() * FORWARD_DRIFT;
        double fy = travel == null ? 0 : travel.getStepY() * FORWARD_DRIFT;
        double fz = travel == null ? 0 : travel.getStepZ() * FORWARD_DRIFT;

        for (int i = 0; i < PARTICLES_PER_RING; i++) {
            double angle = (Math.PI * 2.0 * i) / PARTICLES_PER_RING;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double ox = u.x * cos + v.x * sin;
            double oy = u.y * cos + v.y * sin;
            double oz = u.z * cos + v.z * sin;
            level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    cx + ox * RING_START_RADIUS, cy + oy * RING_START_RADIUS, cz + oz * RING_START_RADIUS,
                    ox * RING_SPEED + fx, oy * RING_SPEED + fy, oz * RING_SPEED + fz);
        }
    }
}
