package com.relicbound.sound;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * One node of a sound wavefront.
 *
 * @param position          block this node occupies
 * @param incomingDirection direction of TRAVEL that carried the wave into this block
 *                          (null for the origin node)
 * @param energy            remaining energy after entering this block
 * @param bounceCount       prism bounces so far (always 0 until Milestone 3)
 * @param waveId            identifies the pulse this node belongs to
 */
public record WaveNode(BlockPos position,
                       @Nullable Direction incomingDirection,
                       float energy,
                       int bounceCount,
                       UUID waveId) {

    /** Creates the follow-on node for a step into {@code nextPos}, keeping bounce count and wave id. */
    public WaveNode step(BlockPos nextPos, Direction travelDirection, float newEnergy) {
        return new WaveNode(nextPos, travelDirection, newEnergy, bounceCount, waveId);
    }
}
