package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.mob.brain.Intent;
import com.grahambartley.nightborn.mob.brain.IntentGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class WalkHomeGoal extends GoToWalkTargetGoal implements IntentGoal {

  public WalkHomeGoal(final PathAwareEntity mob, final double speed) {
    super(mob, speed);
  }

  @Override
  public Intent intent() {
    return Intent.TRAVEL;
  }
}
