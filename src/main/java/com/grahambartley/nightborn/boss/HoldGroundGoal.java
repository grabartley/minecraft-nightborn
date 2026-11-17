package com.grahambartley.nightborn.boss;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class HoldGroundGoal extends Goal {

  private final MobEntity mob;
  private final BooleanSupplier busy;

  public HoldGroundGoal(final MobEntity mob, final BooleanSupplier busy) {
    this.mob = mob;
    this.busy = busy;
    setControls(EnumSet.of(Control.MOVE, Control.JUMP));
  }

  @Override
  public boolean canStart() {
    return busy.getAsBoolean();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    mob.getNavigation().stop();
  }

  @Override
  public void tick() {
    mob.getNavigation().stop();
    if (mob.getTarget() != null) {
      mob.getLookControl().lookAt(mob.getTarget(), 30.0F, 30.0F);
    }
  }
}
