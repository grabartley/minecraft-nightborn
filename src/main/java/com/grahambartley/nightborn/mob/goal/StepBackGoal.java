package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.mob.Unsticking;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;

public final class StepBackGoal extends Goal {

  private final PathAwareEntity mob;
  private final double speed;

  public StepBackGoal(final PathAwareEntity mob, final double speed) {
    this.mob = mob;
    this.speed = speed;
    setControls(EnumSet.of(Control.MOVE));
  }

  @Override
  public boolean canStart() {
    return Unsticking.backingTo(mob) != null;
  }

  @Override
  public boolean shouldContinue() {
    return Unsticking.backingTo(mob) != null && !mob.getNavigation().isIdle();
  }

  @Override
  public void start() {
    final Vec3d spot = Unsticking.backingTo(mob);
    if (spot != null) {
      mob.getNavigation().startMovingTo(spot.x, spot.y, spot.z, speed);
    }
  }

  @Override
  public void stop() {
    mob.getNavigation().stop();
  }
}
