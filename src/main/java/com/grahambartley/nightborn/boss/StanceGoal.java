package com.grahambartley.nightborn.boss;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public final class StanceGoal extends Goal {

  static final int REPATH_TICKS = 10;
  static final float BACKPEDAL = -0.6F;
  static final float TURN = 30.0F;

  private final MobEntity mob;
  private final Supplier<Optional<PhaseStance>> stance;
  private int age;

  public StanceGoal(final MobEntity mob, final Supplier<Optional<PhaseStance>> stance) {
    this.mob = mob;
    this.stance = stance;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  public static boolean repaths(final int age, final boolean idle) {
    return idle || age % REPATH_TICKS == 0;
  }

  @Override
  public boolean canStart() {
    final LivingEntity target = mob.getTarget();
    return target != null && target.isAlive() && stance.get().isPresent();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    age = 0;
  }

  @Override
  public void stop() {
    mob.getNavigation().stop();
  }

  @Override
  public void tick() {
    final LivingEntity target = mob.getTarget();
    final Optional<PhaseStance> held = stance.get();
    if (target == null || held.isEmpty()) {
      return;
    }
    final PhaseStance now = held.get();
    mob.getLookControl().lookAt(target, TURN, TURN);
    switch (now.move(mob.distanceTo(target))) {
      case CLOSE -> {
        if (repaths(age, mob.getNavigation().isIdle())) {
          mob.getNavigation().startMovingTo(target, now.speed());
        }
      }
      case BACK -> sidestep(target, BACKPEDAL, now.side(age));
      case CIRCLE -> sidestep(target, 0.0F, now.side(age));
    }
    age++;
  }

  private void sidestep(final LivingEntity target, final float forward, final float sideways) {
    mob.getNavigation().stop();
    mob.lookAtEntity(target, TURN, TURN);
    mob.getMoveControl().strafeTo(forward, sideways);
  }
}
