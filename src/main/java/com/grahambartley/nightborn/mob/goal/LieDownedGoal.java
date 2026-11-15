package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.DownedMobRules;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class LieDownedGoal extends Goal {

  private final MobEntity mob;

  public LieDownedGoal(final MobEntity mob) {
    this.mob = mob;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
  }

  @Override
  public boolean canStart() {
    return DownedMobRules.helpless(Downed.state(mob));
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    lieStill();
  }

  @Override
  public void tick() {
    lieStill();
  }

  private void lieStill() {
    mob.getNavigation().stop();
    mob.setTarget(null);
    mob.setAttacking(false);
  }
}
