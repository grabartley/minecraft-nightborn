package com.grahambartley.nightborn.guild;

import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public final class Footing {

  public static final float SHUNNED = -1.0F;

  private Footing() {}

  public static boolean standable(final PathNodeType feet, final PathNodeType floor) {
    return feet != PathNodeType.FENCE
        && feet != PathNodeType.BLOCKED
        && floor != PathNodeType.FENCE;
  }

  public static float favor(final MobEntity mob, final BlockPos pos, final WorldView world) {
    final PathContext context = new PathContext(world, mob);
    return standable(
            LandPathNodeMaker.getLandNodeType(context, pos.mutableCopy()),
            LandPathNodeMaker.getLandNodeType(context, pos.down().mutableCopy()))
        ? 0.0F
        : SHUNNED;
  }
}
