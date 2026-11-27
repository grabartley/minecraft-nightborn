package com.grahambartley.nightborn.guild;

import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import org.jetbrains.annotations.Nullable;

public final class StallWatch {

  private final StallCount count = new StallCount();

  public void watch(final PathAwareEntity mob, @Nullable final WanderAroundGoal wander) {
    final EntityNavigation navigation = mob.getNavigation();
    final boolean walking = !navigation.isIdle() && mob.getTarget() == null;
    if (count.gaveUp(walking ? navigation.getTargetPos() : null, mob.getPos())) {
      navigation.stop();
      if (wander != null) {
        wander.ignoreChanceOnce();
      }
    }
  }
}
