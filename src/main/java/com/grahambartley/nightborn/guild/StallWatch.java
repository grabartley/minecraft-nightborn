package com.grahambartley.nightborn.guild;

import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class StallWatch {

  private final StallCount count = new StallCount();

  public void watch(final PathAwareEntity mob, @Nullable final WanderAroundGoal wander) {
    final EntityNavigation navigation = mob.getNavigation();
    final Path path = navigation.getCurrentPath();
    final boolean walking =
        path != null && !path.isFinished() && !navigation.isIdle() && mob.getTarget() == null;
    final BlockPos heading = walking ? navigation.getTargetPos() : null;
    final double toNode =
        walking ? Vec3d.ofBottomCenter(path.getCurrentNodePos()).distanceTo(mob.getPos()) : 0.0;
    if (count.gaveUp(heading, walking ? path.getCurrentNodeIndex() : 0, toNode)) {
      navigation.stop();
      if (wander != null) {
        wander.ignoreChanceOnce();
      }
    }
  }
}
