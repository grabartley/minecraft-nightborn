package com.grahambartley.nightborn.guild;

import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class StallWatch {

  public static final int GIVE_UP_TICKS = 60;
  public static final double PROGRESS = 0.25;

  @Nullable private BlockPos target;
  private double best;
  private int stalled;

  public boolean gaveUp(@Nullable final BlockPos heading, final double distance) {
    if (heading == null || !heading.equals(target)) {
      target = heading;
      best = distance;
      stalled = 0;
      return false;
    }
    if (distance < best - PROGRESS) {
      best = distance;
      stalled = 0;
      return false;
    }
    if (++stalled < GIVE_UP_TICKS) {
      return false;
    }
    target = null;
    stalled = 0;
    return true;
  }

  public void watch(final PathAwareEntity mob, final WanderAroundGoal wander) {
    final EntityNavigation navigation = mob.getNavigation();
    final BlockPos heading =
        navigation.isIdle() || mob.getTarget() != null ? null : navigation.getTargetPos();
    final double distance =
        heading == null ? 0.0 : Math.sqrt(heading.getSquaredDistance(mob.getPos()));
    if (gaveUp(heading, distance)) {
      navigation.stop();
      wander.ignoreChanceOnce();
    }
  }
}
