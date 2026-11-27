package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.mob.Unsticking;
import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;

public final class StepBackGoal extends Goal {

  private final PathAwareEntity mob;
  private final double speed;
  private final BooleanSupplier escaping;

  public StepBackGoal(
      final PathAwareEntity mob, final double speed, final BooleanSupplier escaping) {
    this.mob = mob;
    this.speed = speed;
    this.escaping = escaping;
    setControls(EnumSet.of(Control.MOVE));
  }

  @Override
  public boolean canStart() {
    return !escaping.getAsBoolean() && Unsticking.backingTo(mob) != null;
  }

  @Override
  public boolean shouldContinue() {
    return !escaping.getAsBoolean()
        && Unsticking.backingTo(mob) != null
        && !mob.getNavigation().isIdle();
  }

  @Override
  public void start() {
    final Vec3d spot = Unsticking.backingTo(mob);
    if (spot == null || !mob.getNavigation().startMovingTo(spot.x, spot.y, spot.z, speed)) {
      Unsticking.backedOff(mob);
    }
  }

  @Override
  public void stop() {
    Unsticking.backedOff(mob);
    mob.getNavigation().stop();
  }
}
