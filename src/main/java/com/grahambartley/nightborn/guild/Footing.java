package com.grahambartley.nightborn.guild;

import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

public final class Footing {

  public static final float SHUNNED = -1.0F;

  private Footing() {}

  public static boolean standable(final PathNodeType feet, final PathNodeType floor) {
    return feet != PathNodeType.FENCE
        && feet != PathNodeType.BLOCKED
        && floor != PathNodeType.FENCE;
  }

  public static float favor(final MobEntity mob, final BlockPos pos) {
    return standable(
            LandPathNodeMaker.getLandNodeType(mob, pos),
            LandPathNodeMaker.getLandNodeType(mob, pos.down()))
        ? 0.0F
        : SHUNNED;
  }
}
